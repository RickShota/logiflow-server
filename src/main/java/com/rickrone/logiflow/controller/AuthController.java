package com.rickrone.logiflow.controller;

import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.common.utils.JwtUtils;
import com.rickrone.logiflow.model.dto.LoginDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;

  @PostMapping("/login")
  public Result<Map<String, String>> login(@RequestBody @Valid LoginDTO loginDTO) {
    // 1. 使用 AuthenticationManager 进行用户认证（会自动调用 UserDetailsServiceImpl 和 BCrypt 校验密码）
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
    );

    // 2. 认证通过后，获取 UserDetails
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // 3. 生成 JWT Token
    String token = jwtUtils.generateToken(userDetails);

    // 4. 将 Token 封装返回给前端
    Map<String, String> data = new HashMap<>();
    data.put("token", token);
    data.put("tokenHead", "Bearer ");

    return Result.success(data, "登录成功");
  }
}
