package com.rickrone.logiflow.config;

import com.rickrone.logiflow.security.JwtAuthenticationFilter;
import com.rickrone.logiflow.security.RestAccessDeniedHandler;
import com.rickrone.logiflow.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置。
 *
 * <p>当前阶段（登录模块完成前）放行全部接口，仅用于本地调试。
 * 登录/JWT 做好后，把 anyRequest() 改为 authenticated()，
 * 并在此处注册 JWT 过滤器、放行 /auth/login。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;

  /**
   * 1. 密码加密器：使用高强度的 BCrypt 算法
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 2. 安全过滤链核心配置
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // 禁用 CSRF（前后端分离项目使用 Token，不需要 CSRF 防护）
            .csrf(AbstractHttpConfigurer::disable)

            // 设置 Session 策略为无状态 (Stateless)，因为我们用 JWT，不使用 HttpSession
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 统一异常响应：未登录返回 401、权限不足返回 403
            // 这类异常发生在过滤器链中，不会经过 @RestControllerAdvice，需单独配置
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler)
            )

            // 请求授权配置
            .authorizeHttpRequests(auth -> auth
                    // 放行白名单（如登录接口、注册接口、以及我们刚才写的测试接口）
                    .requestMatchers("/api/auth/**", "/test/**").permitAll()
                    // 其他所有请求都需要鉴权认证
                    .anyRequest().authenticated()
            )

            // 设置 AuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // 将我们的 JWT 过滤器加在 Spring Security 默认的用户名密码认证过滤器之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
  /**
   * 3. 认证 Provider 配置
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * 4. 暴露 AuthenticationManager 供登录接口使用
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
