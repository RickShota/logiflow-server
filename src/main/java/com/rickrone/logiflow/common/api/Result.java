package com.rickrone.logiflow.common.api;
// 统一响应体封装
import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
  private long code;
  private String msg;
  private T data;

  protected Result() {}

  protected Result(long code, String msg, T data) {
    this.code = code;
    this.msg = msg;
    this.data = data;
  }

  /**
   * 成功返回 (带数据)
   */
  public static <T> Result<T> success(T data) {
    return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
  }

  /**
   * 成功返回 (带自定义提示信息和数据)
   */
  public static <T> Result<T> success(T data, String message) {
    return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
  }

  /**
   * 失败返回 (自定义错误信息)
   */
  public static <T> Result<T> failed(String message) {
    return new Result<>(ResultCode.FAILED.getCode(), message, null);
  }

  /**
   * 失败返回 (使用 ResultCode 枚举)
   */
  public static <T> Result<T> failed(ResultCode errorCode) {
    return new Result<>(errorCode.getCode(), errorCode.getMsg(), null);
  }

  /**
   * 失败返回 (自定义状态码和错误信息)
   */
  public static <T> Result<T> failed(long code, String message) {
    return new Result<>(code, message, null);
  }
}
