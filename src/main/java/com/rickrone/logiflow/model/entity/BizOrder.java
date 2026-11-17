package com.rickrone.logiflow.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_order")
public class BizOrder {
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  private String orderNo;   // 全局唯一订单流水号（MQ 幂等防重唯一索引）

  private Long userId;      // 申领人 ID

  private Long materialId;  // 申领物资 ID

  private Integer quantity; // 申领数量

  private Integer status;   // 0-排队处理中，1-已完成(已扣库存)，2-库存不足失败，3-超时关闭

  private String remark;    // 申领备注

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
