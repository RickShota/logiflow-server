package com.rickrone.logiflow.security;

import tools.jackson.databind.ObjectMapper;
import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 已认证但权限不足时的统一响应（同样发生在过滤器层）。
 *
 * <p>返回 HTTP 403，响应体为统一 Result 结构。
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request,
                     HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), Result.failed(ResultCode.FORBIDDEN));
  }
}
