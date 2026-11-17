package com.rickrone.logiflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rickrone.logiflow.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;


@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

  /**
   * 根据用户ID查询其拥有的所有权限标识符（对接 RBAC 五表模型）
   * 这里展示了 MyBatis-Plus 混血用法：单表用 BaseMapper，多表 JOIN 直接写原生 SQL
   */
  @Select("""
          SELECT DISTINCT p.perm_code
          FROM sys_permission p
          INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
          INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
          WHERE ur.user_id = #{userId}
            AND p.perm_code IS NOT NULL
            AND p.perm_code != ''
          """)
  List<String> selectPermCodesByUserId(@Param("userId") Long userId);
}

