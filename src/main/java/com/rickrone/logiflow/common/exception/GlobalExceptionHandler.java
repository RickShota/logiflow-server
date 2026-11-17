package com.rickrone.logiflow.common.exception;

import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
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
   * 4. 系统未知异常兜底 (如数据库挂了、空指针等)
   */
  @ExceptionHandler(Exception.class)
  public Result<Void> handleException(Exception e) {
    log.error("系统未知严重异常: ", e);
    return Result.failed(ResultCode.FAILED.getCode(), "系统开小差了，请稍后再试");
  }
}
