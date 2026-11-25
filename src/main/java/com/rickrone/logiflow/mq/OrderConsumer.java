package com.rickrone.logiflow.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import com.rickrone.logiflow.config.RabbitMQConfig;
import com.rickrone.logiflow.mapper.BizMaterialMapper;
import com.rickrone.logiflow.mapper.BizOrderMapper;
import com.rickrone.logiflow.model.entity.BizMaterial;
import com.rickrone.logiflow.model.entity.BizOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {
  private static final String IDEMPOTENT_KEY_PREFIX = "logiflow:mq:order:consumed:";
  private static final String RETRY_KEY_PREFIX = "logiflow:mq:order:retry:";
  /** 单条消息最多重试次数，超过后不再重新入队，避免确定性错误造成的死循环 */
  private static final int MAX_RETRY = 3;
  private final StringRedisTemplate stringRedisTemplate;
  private final BizMaterialMapper bizMaterialMapper;
  private final BizOrderMapper bizOrderMapper;

  @RabbitListener(queues = RabbitMQConfig.ORDER_CREATE_QUEUE)
  @Transactional(rollbackFor = Exception.class)
  public void handleOrderCreate(OrderMessage msg, Message message, Channel channel) throws IOException {
    long deliveryTag = message.getMessageProperties().getDeliveryTag();
    String orderNo = msg.getOrderNo();
    String redisKey = IDEMPOTENT_KEY_PREFIX + orderNo;

    try {
      // 1. Redis SETNX 幂等防重：24小时内相同 orderNo 只允许处理一次
      Boolean firstConsume = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofHours(24));
      if (Boolean.FALSE.equals(firstConsume)) {
        log.warn("检测到重复消息，已跳过处理，orderNo={}", orderNo);
        channel.basicAck(deliveryTag, false);
        return;
      }
      // 2. 查询订单，避免重复完成
      BizOrder order = bizOrderMapper.selectOne(
              new LambdaQueryWrapper<BizOrder>().eq(BizOrder::getOrderNo, orderNo)
      );
      if (order == null) {
        log.info("订单不存在或已处理，直接确认消息，orderNo={}", orderNo);
        channel.basicAck(deliveryTag, false);
        return;
      }
      // 幂等兜底：只有排队中（status=0）的订单才需要扣库存。
      // 消息重投时若订单已是完成/失败/关闭状态，直接确认，避免重复扣减
      if (order.getStatus() != 0) {
        log.info("订单已处理（status={}），跳过重复消费，orderNo={}", order.getStatus(), orderNo);
        channel.basicAck(deliveryTag, false);
        return;
      }
      // 3. 原子条件扣减库存（stock >= quantity 才扣减，防止超卖）
      boolean deducted = bizMaterialMapper.update(
              null,
              new LambdaUpdateWrapper<BizMaterial>()
                      .eq(BizMaterial::getId, msg.getMaterialId())
                      .ge(BizMaterial::getStock, msg.getQuantity())
                      .setSql("stock = stock - {0}", msg.getQuantity())
      ) > 0;
      if (deducted) {
        order.setStatus(1); // 1-已完成（已扣库存）
        bizOrderMapper.updateById(order);
        log.info("订单库存扣减成功，orderNo={}", orderNo);
      } else {
        order.setStatus(2); // 2-库存不足失败
        order.setRemark("异步扣减时库存不足");
        bizOrderMapper.updateById(order);
        log.warn("订单库存不足，标记失败，orderNo={}", orderNo);
      }
      // 4. 手动 ACK 确认消息已安全处理
      channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
      log.error("处理订单消息异常，orderNo={}，准备重试", orderNo, e);
      // 异常时把事务标记为回滚，避免"部分写库成功 + 消息重投"造成重复扣减
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      // 清理防重 Key 以便后续重试能正常进入业务判断
      stringRedisTemplate.delete(redisKey);
      // 统计重试次数：没有上限时，确定性异常（代码 bug、脏数据等）会让消息被无限重投，
      // 表现为 CPU 占满、日志暴涨，即"毒消息"死循环
      Long retryCount = stringRedisTemplate.opsForValue().increment(RETRY_KEY_PREFIX + orderNo);
      stringRedisTemplate.expire(RETRY_KEY_PREFIX + orderNo, Duration.ofHours(1));
      if (retryCount != null && retryCount > MAX_RETRY) {
        log.error("订单消息重试次数已达上限（{} 次），停止重新入队，订单保持排队状态待人工处理，orderNo={}",
                MAX_RETRY, orderNo);
        stringRedisTemplate.delete(RETRY_KEY_PREFIX + orderNo);
        // 项目暂未配置死信队列，requeue = false 会丢弃消息；后续可改为转发到死信队列
        channel.basicNack(deliveryTag, false, false);
        return;
      }
      // requeue = true 让消息重新回到队列尝试消费
      channel.basicNack(deliveryTag, false, true);
    }
  }
}
