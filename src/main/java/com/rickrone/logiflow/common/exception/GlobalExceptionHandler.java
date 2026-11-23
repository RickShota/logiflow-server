package com.rickrone.logiflow.common.exception;

import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  /**
   * 1. 捕获自定义业务异常 (如：throw new BusinessException("库存不足"));
   */
  @ExceptionHandler(BusinessException.class)
  public Result<Void> handleBusinessException(BusinessException e) {
    log.warn("业务异常拦截: {}", e.getMessage());
    return Result.failed(e.getCode(), e.getMessage());
  }

  /**
   * 2. 捕获 DTO 参数校验失败异常 (@Valid 校验失败)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleValidException(MethodArgumentNotValidException e) {
    FieldError fieldError = e.getBindingResult().getFieldError();
    String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
    log.warn("参数校验异常: {}", message);
    return Result.failed(ResultCode.VALIDATE_FAILED.getCode(), message);
  }

  /**
   * 3. 捕获表单绑定校验异常
   */
  @ExceptionHandler(BindException.class)
  public Result<Void> handleBindException(BindException e) {
    FieldError fieldError = e.getBindingResult().getFieldError();
    String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
    log.warn("参数绑定异常: {}", message);
    return Result.failed(ResultCode.VALIDATE_FAILED.getCode(), message);
  }

  /**
   * 4. 登录认证失败（AuthenticationManager.authenticate 抛出）
   *    必须单独拦截，否则会落入 Exception 兜底，被当成系统异常返回 500
   */
  @ExceptionHandler(AuthenticationException.class)
  public Result<Void> handleAuthenticationException(AuthenticationException e) {
    // 账号被禁用：给出明确提示
    if (e instanceof DisabledException) {
      log.warn("账号已被禁用: {}", e.getMessage());
      return Result.failed(ResultCode.ACCOUNT_DISABLED);
    }
    // 认证过程本身出错（如数据库不可用），仍按系统异常处理，避免掩盖真实故障
    if (e instanceof InternalAuthenticationServiceException) {
      log.error("认证服务内部异常: ", e);
      return Result.failed(ResultCode.FAILED.getCode(), "系统开小差了，请稍后再试");
    }
    // 用户名不存在或密码错误：统一提示，防止用户名枚举
    log.warn("登录认证失败: {}", e.getMessage());
    return Result.failed(ResultCode.LOGIN_FAILED);
  }

  /**
   * 5. 已认证但权限不足（@PreAuthorize 拒绝时抛出 AuthorizationDeniedException）
   */
  @ExceptionHandler(AccessDeniedException.class)
  public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
    log.warn("权限不足: {}", e.getMessage());
    return Result.failed(ResultCode.FORBIDDEN);
  }

  /**
   * 6. 系统未知异常兜底 (如数据库挂了、空指针等)
   */
  @ExceptionHandler(Exception.class)
  public Result<Void> handleException(Exception e) {
    log.error("系统未知严重异常: ", e);
    return Result.failed(ResultCode.FAILED.getCode(), "系统开小差了，请稍后再试");
  }
}
