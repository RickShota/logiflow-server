package com.rickrone.logiflow.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

@Data
@TableName("biz_material")
public class BizMaterial {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  private String materialCode; // 物资编码

  private String materialName; // 物资名称

  private String category;     // 物资分类

  private Integer stock;       // 当前可用库存

  @Version
  private Integer version;     // 乐观锁版本号（高并发防超卖核心）

  @TableLogic
  private Integer deleted;     // 逻辑删除：0-未删，1-已删

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
