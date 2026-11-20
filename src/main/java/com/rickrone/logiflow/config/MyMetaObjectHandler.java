package com.rickrone.logiflow.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器。
 *
 * <p>实体上的 {@code @TableField(fill = ...)} 只是标记"该字段需要自动填充"，
 * 真正地赋值由本处理器完成。若没有这个 Bean，createTime/updateTime 会以 null
 * 参与 INSERT，触发数据库的 NOT NULL 约束错误。
 *
 * <p>覆盖所有带 fill 标记的实体（SysUser / BizMaterial / BizOrder）。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

  /**
   * 插入时填充：创建时间与更新时间都取当前时间。
   */
  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now();
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
    this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
  }

  /**
   * 更新时填充：只刷新更新时间。
   */
  @Override
  public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
  }
}
