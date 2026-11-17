package com.rickrone.logiflow.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rickrone.logiflow.mapper.SysUserMapper;
import com.rickrone.logiflow.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final SysUserMapper sysUserMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1. 从数据库中查询用户
    SysUser sysUser = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
    );
    if (sysUser == null) {
      throw new UsernameNotFoundException("用户名或密码错误");
    }
    // 2. 加载该用户的权限标识
    List<String> permCodes = sysUserMapper.selectPermCodesByUserId(sysUser.getId());
    List<GrantedAuthority> authorities = new ArrayList<>();

    if (permCodes != null && !permCodes.isEmpty()) {
      for (String code : permCodes) {
        authorities.add(new SimpleGrantedAuthority(code));
      }
    } else {
      // 数据库未分配权限时的默认测试权限
      authorities.add(new SimpleGrantedAuthority("sys:order:add"));
    }
    // 3. 返回 Spring Security 自带的 User 对象（包含账号、BCrypt加密后的密码、权限）
    return new User(
            sysUser.getUsername(),
            sysUser.getPassword(),
            sysUser.getStatus() == 1, // 是否启用
            true,
            true,
            true,
            authorities
    );
  }
}
