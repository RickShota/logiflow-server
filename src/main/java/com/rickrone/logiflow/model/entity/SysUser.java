package com.rickrone.logiflow.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  private String username;

  private String password; // 存储 BCrypt 加密后的密文

  private String nickname;

  private Integer status; // 1-正常，0-禁用

  @TableLogic
  private Integer deleted; // 逻辑删除：0-未删，1-已删

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
