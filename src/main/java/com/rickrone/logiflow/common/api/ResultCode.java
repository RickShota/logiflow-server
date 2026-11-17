package com.rickrone.logiflow.common.api;

public enum ResultCode {
  SUCCESS(200, "操作成功"),
  FAILED(500, "系统内部异常"),
  UNAUTHORIZED(401, "暂未登录或Token已过期"),
  FORBIDDEN(403, "没有相关权限，拒绝访问"),
  VALIDATE_FAILED(400, "参数校验失败");

  private final long code;
  private final String msg;

  ResultCode(long code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public long getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }
}
