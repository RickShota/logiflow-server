package com.rickrone.logiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// 排除默认的内置用户自动配置：本项目使用 JWT + 数据库用户体系，
// 避免启动时生成无用的内存用户和随机密码。
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class LogiflowServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(LogiflowServerApplication.class, args);
  }

}
