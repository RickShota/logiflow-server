package com.rickrone.logiflow.controller;

import com.rickrone.logiflow.common.api.Result;
import com.rickrone.logiflow.common.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

  @GetMapping("/hello")
  public Result<String> hello(@RequestParam(value = "name") String name) {
    return Result.success("你好！" + name);
  }

  @GetMapping("/biz-error")
  public Result<Void> testBizError() {
    // 主动抛出业务异常，测试全局异常处理器是否能优雅捕获
    throw new BusinessException(4001, "测试：自定义业务异常拦截成功！");
  }

  @GetMapping("/sys-error")
  public Result<Void> testSysError() {
    // 模拟除零错误或空指针，触发 Exception.class 兜底
    int i = 1 / 0;
    return Result.success(null);
  }
}
