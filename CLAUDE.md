# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 协作背景

项目所有者是前端转全栈的开发者,这是他的练手项目,会频繁提问。解释后端概念时可以用前端/TypeScript 类比,但不要假设他熟悉 Java/Spring 生态的惯例;遇到底层机制(事务、代理、消息确认等)要讲清"为什么"。

## 项目定位

LogiFlow:企业级物资与订单流转系统,后端串联 Spring Boot + MySQL + Redis + RabbitMQ + Docker + Spring Security,前端管理后台位于 `frontend/`(Vue 3 + TypeScript + Vite + Naive UI)。完整蓝图(技术栈映射、4 个业务闭环、4 阶段路线图)见 `docs/BLUEPRINT.md`,需求方向有疑问时以它为准。

## 常用命令

```bash
./mvnw compile                  # 编译
./mvnw spring-boot:run          # 启动(依赖下方三个中间件)
./mvnw test                     # 全部测试
./mvnw test -Dtest=LogiflowServerApplicationTests#contextLoads  # 单个测试
./mvnw package                  # 打包到 target/logiflow-0.0.1-SNAPSHOT.jar
```

前端(在 `frontend/` 下执行):

```bash
npm install                     # 安装依赖
npm run dev                     # 开发服务器 5173,`/api` 代理到 8080
npm run build                   # 类型检查 + 生产构建
```

数据库初始化(建库 + 7 张表):

```bash
mysql -uroot -h127.0.0.1 < src/main/resources/db/init.sql
```

## 本地依赖服务

均通过 Homebrew 安装并由 `brew services` 托管(`brew services list` 查看状态):

| 服务 | 连接信息 |
|---|---|
| MySQL 8.0 | localhost:3306,root 无密码,库 `logiflow_db` |
| Redis | localhost:6379,无密码 |
| RabbitMQ | localhost:5672,guest/guest,管理台 http://localhost:15672 |

连接配置在 `src/main/resources/application.yml`。`@SpringBootTest` 的 `contextLoads` 需要三个服务都在运行;Redis/RabbitMQ 未启动时应用初始化阶段会失败。

## 技术栈要点

- Java 21 + **Spring Boot 4.1.2**。注意是 Boot 4:Web starter 叫 `spring-boot-starter-webmvc`(不是 3.x 的 `spring-boot-starter-web`),Servlet 容器为 Tomcat 11。不要按 Spring Boot 3 的依赖名或 API 习惯写代码。
- MyBatis-Plus 3.5.17,使用 Boot 4 专用的 `mybatis-plus-spring-boot4-starter`(3.5.5 的 boot3 starter 在 Boot 4 下自动配置不生效,会导致 Mapper 无法注册)。
- Spring Security 已配置无状态 JWT 认证 + RBAC 权限加载(见 `SecurityConfig` / `JwtAuthenticationFilter` / `UserDetailsServiceImpl`),`/api/auth/**` 与 `/test/**` 为白名单。
- Lombok 在 `maven-compiler-plugin` 中显式声明了注解处理器,命令行编译与 IDE 均可用。
- 前端 `frontend/`:Vue 3 + TypeScript + Vite 8 + Naive UI,组件经 `unplugin-vue-components` 按需自动导入;开发代理在 `vite.config.ts`(将 `/api` 转发到 `localhost:8080`),尚未接入 Nginx 与 Compose 前端服务。

## 代码结构与约定

包根 `com.rickrone.logiflow`。已建立的统一返回体系在 `common/api`:

- `Result<T>`:统一响应体(`code`/`msg`/`data`),构造器非 public,只能通过静态工厂创建;新增 `fail`/`of` 等方法时保持这一模式。
- `ResultCode`:错误码枚举,数字沿用 HTTP 语义(200/400/401/403/500)。新增错误码加在此枚举,不要在业务代码里硬编码数字。

数据库 schema(`src/main/resources/db/init.sql`)是理解业务模型的入口,关键约定:

- RBAC 五表:`sys_user` / `sys_role` / `sys_permission` / `sys_user_role` / `sys_role_permission`;权限标识格式为 `sys:order:add`,配合 `@PreAuthorize` 使用。
- 业务两表:`biz_material`(库存,`version` 字段用于乐观锁防超卖)、`biz_order`(申领订单)。
- 业务表均带 `deleted` 逻辑删除字段,MyBatis-Plus 全局已配置 `logic-delete-field: deleted`,查询自动过滤,勿手写 `deleted = 0`。
- `biz_order.uk_order_no` 唯一索引是 MQ 消费幂等的数据库兜底,不可移除。
- 订单状态:0-排队处理中,1-已完成(已扣库存),2-库存不足失败,3-超时关闭。

README 规划的分层规范:Controller / Service / DAO 三层,DTO、Entity、VO 严格分离,入参用 `@Valid` 校验。

核心业务链路(申领下单,路线图第三阶段):插入订单 → 发 RabbitMQ 消息 → 消费者以 Redis `SETNX` + 数据库唯一索引双重幂等 → 扣库存 → 手动 `basicAck`。涉及此链路时保持"生产端确认 + 手动 ACK"的设计。

## 环境注意

- 代码风格:2 空格缩进,注释用中文。
- 本机为 macOS (Apple Silicon),shell 为 zsh。
