package com.rickrone.logiflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rickrone.logiflow.common.exception.BusinessException;
import com.rickrone.logiflow.mapper.BizMaterialMapper;
import com.rickrone.logiflow.mapper.BizOrderMapper;
import com.rickrone.logiflow.mapper.SysUserMapper;
import com.rickrone.logiflow.model.dto.OrderCreateDTO;
import com.rickrone.logiflow.model.entity.BizMaterial;
import com.rickrone.logiflow.model.entity.SysUser;
import com.rickrone.logiflow.mq.OrderProducer;
import com.rickrone.logiflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
  private final BizOrderMapper bizOrderMapper;
  private final BizMaterialMapper bizMaterialMapper;
  private final SysUserMapper sysUserMapper;
  private final OrderProducer orderProducer;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public String submitOrder(OrderCreateDTO dto, String username) {
    // 1.查当前用户
    SysUser user = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
    );
    if (user == null) {
      throw new BusinessException("用户不存在");
    }
    // 2.校验物资与库存初筛
    BizMaterial material = bizMaterialMapper.selectById(dto.getMaterialId());
    if (material == null) {
      throw new BusinessException("物资不存在");
    }
    if (material.getStock() < dto.getQuantity()) {
      throw new BusinessException("当前物资库存不足，剩余库存：" + material.getStock());
    }
    // 3.生成唯一订单号并写入初始订单（状态 0：排队处理中）

    // 4.发送异步消息到 RabbitMQ 进行库存扣减
    return "";
  }
}
