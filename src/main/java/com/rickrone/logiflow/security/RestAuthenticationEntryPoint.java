package com.rickrone.logiflow.security;

import tools.jackson.databind.ObjectMapper;
import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证访问受保护接口时的统一响应。
 *
 * <p>该类处理的是「在过滤器链中被拦下」的请求，此时还没进入 DispatcherServlet，
 * 不会经过 @RestControllerAdvice，所以需要在这里手动写出统一 Result 结构。
 * 返回 HTTP 401（响应体 code 同为 401）。
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), Result.failed(ResultCode.UNAUTHORIZED));
  }
}
