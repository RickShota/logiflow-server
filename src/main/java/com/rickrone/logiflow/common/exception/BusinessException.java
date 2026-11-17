package com.rickrone.logiflow.common.exception;

import com.rickrone.logiflow.common.api.ResultCode;

/**
 * 自定义业务异常。
 *
 * <p>用于表达"业务规则不满足"这类可预期的失败(如库存不足、账号已存在、无权限),
 * 与程序 bug 导致的系统异常区分开。业务代码里直接 {@code throw new BusinessException(...)},
 * 由全局异常处理器统一捕获,并把错误码和提示信息转换为 Result 响应体返回前端。
 *
 * <p>错误码来源有两种:传 {@link ResultCode} 枚举(推荐),或自定义 code + message。
 */
public class BusinessException extends RuntimeException {

  /** 业务错误码,数值沿用 HTTP 语义(200/400/401/403/500),随响应体返回给前端 */
  private final long code;

  /**
   * 使用默认失败码({@link ResultCode#FAILED},即 500)和自定义提示信息。
   *
   * @param message 返回给前端的错误提示
   */
  public BusinessException(String message) {
    super(message);
    this.code = ResultCode.FAILED.getCode();
  }

  /**
   * 使用枚举提供的错误码和提示信息(推荐用法)。
   *
   * @param resultCode 错误码枚举,如 {@link ResultCode#UNAUTHORIZED}
   */
  public BusinessException(ResultCode resultCode) {
    super(resultCode.getMsg());
    this.code = resultCode.getCode();
  }

  /**
   * 完全自定义错误码和提示信息,用于枚举未覆盖的场景。
   *
   * @param code    自定义业务错误码
   * @param message 返回给前端的错误提示
   */
  public BusinessException(long code, String message) {
    super(message);
    this.code = code;
  }

  /** 获取业务错误码,供全局异常处理器写入统一响应体 */
  public long getCode() {
    return code;
  }
}
