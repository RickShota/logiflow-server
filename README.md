<div align="center">

# LogiFlow

**企业级物资与订单流转系统**

一条「申领下单」链路，串起 JWT 认证、RBAC 权限、乐观锁、Redis 幂等与 RabbitMQ 异步扣库存。

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

</div>

> 🚧 **开发状态**：项目处于活跃开发中。工程骨架、统一响应、全局异常与 JWT + RBAC 认证已完成；物资管理、异步下单链路与容器化部署正在推进，详见下方路线图。

---

## ✨ 项目简介

LogiFlow（**Logi**stics + **Flow**）是一个面向企业内部的物资申领与订单流转系统：员工登录后提交物资申领单，系统先在事务内落单，再通过 RabbitMQ 异步扣减库存（异步链路规划中），并以 **Redis `SETNX` + 数据库唯一索引** 双重幂等，保证消息重复消费也不会重复扣库存。

除了业务本身，这个项目更想完整演示一套「生产级后端」的工程结构：

| 特性 | 实现方式 |
| --- | --- |
| 🔐 无状态认证 | Spring Security + JWT，登录签发 HS256 Token，请求经自定义过滤器校验后写入 `SecurityContext` |
| 🛂 细粒度权限 | RBAC 五表模型，按用户聚合权限标识，支持 `@PreAuthorize("hasAuthority('sys:order:add')")` |
| 🧱 统一 API 契约 | `Result<T>` + `ResultCode` 枚举 + `@RestControllerAdvice`，前端只需处理一种响应结构 |
| 🛡️ 防超卖 | 库存表 `version` 乐观锁，配合业务唯一索引兜底 |
| 🔁 消费幂等 | Redis `SETNX` 去重 + `biz_order.uk_order_no` 唯一索引双保险（规划中） |
| 📨 可靠投递 | 生产者 Confirm + 消费者手动 ACK（规划中） |
| 🧹 数据治理 | MyBatis-Plus 全局逻辑删除，查询自动过滤，避免手写 `deleted = 0` |
| 🧩 分层规范 | Controller / Service / DAO 三层，DTO / Entity / VO 严格分离，入参 `@Valid` 校验 |

## 🧰 技术栈

| 分类 | 技术 | 用途 |
| --- | --- | --- |
| 语言与运行时 | Java 21 | 主语言，使用文本块等新语法 |
| 应用框架 | Spring Boot 4.1.2 | 自动配置、内嵌 Tomcat 11 |
| Web 层 | Spring WebMVC | RESTful API、参数绑定与校验 |
| 安全 | Spring Security + JJWT 0.11.5 | 无状态认证、BCrypt 密码加密、RBAC 鉴权 |
| 持久层 | MyBatis-Plus 3.5.17 + MySQL 8.0 | 单表 CRUD、分页、逻辑删除、乐观锁 |
| 缓存 | Redis（Lettuce 客户端） | 幂等 SETNX、热点数据缓存（规划） |
| 消息 | RabbitMQ + Spring AMQP | 异步扣库存、延时关单（规划） |
| 构建 | Maven Wrapper | 无需预装 Maven，`./mvnw` 开箱即用 |
| 部署 | Docker + Docker Compose | 多阶段构建与一键编排（规划） |

> 💡 **Spring Boot 4 小贴士**：Web starter 已更名为 `spring-boot-starter-webmvc`（3.x 中为 `spring-boot-starter-web`），Servlet 容器为 Tomcat 11；持久层需使用 `mybatis-plus-spring-boot4-starter`，旧的 boot3 starter 在 Boot 4 下不会注册 Mapper。本文档与代码均按 Boot 4 的依赖习惯编写。

## 🏗️ 系统架构

目标架构如下，**虚线部分为规划中的异步链路**：

```mermaid
flowchart LR
    subgraph Client["客户端"]
        FE["Vue 3 管理后台<br/>（规划中）"]
        CLI["curl / Postman"]
    end

    subgraph Server["Spring Boot 应用"]
        SEC["Spring Security<br/>JWT 认证过滤器"]
        CTRL["Controller<br/>DTO + @Valid"]
        SVC["Service<br/>事务边界"]
        MAPPER["MyBatis-Plus<br/>Mapper"]
        PROD["消息生产者<br/>（规划中）"]
        CONS["库存消费者<br/>手动 ACK（规划中）"]
    end

    subgraph Infra["基础设施"]
        DB[("MySQL 8.0")]
        CACHE[("Redis")]
        BROKER[["RabbitMQ"]]
    end

    FE --> SEC
    CLI --> SEC
    SEC --> CTRL --> SVC
    SVC --> MAPPER --> DB
    SVC -.-> PROD -.-> BROKER
    BROKER -.-> CONS
    CONS -.-> CACHE
    CONS -.-> MAPPER
```

## 🔁 核心链路：异步下单（规划中）

> 以下为第三阶段的设计方案，代码尚未实现，接口路径仅作示意。

```mermaid
sequenceDiagram
    autonumber
    participant U as 客户端
    participant API as OrderController
    participant SVC as OrderService
    participant DB as MySQL
    participant MQ as RabbitMQ
    participant CON as StockConsumer
    participant R as Redis

    U->>API: POST /api/orders（物资 + 数量）
    API->>SVC: createOrder(dto)
    SVC->>DB: INSERT biz_order（status=0 排队中）
    SVC->>MQ: 投递 order.created（publisher confirm）
    SVC-->>U: 返回 orderNo
    MQ-->>CON: 消费消息
    CON->>R: SETNX idempotent:{orderNo}
    alt 首次消费
        CON->>DB: UPDATE biz_material<br/>stock = stock - qty（version 乐观锁）
        CON->>DB: UPDATE biz_order SET status = 1
        CON-->>MQ: basicAck
    else 重复消费
        CON-->>MQ: basicAck（直接丢弃）
    end
```

要点：

1. **先落单、后扣减**：订单在本地事务中插入（状态 `0`），库存扣减交给 MQ 异步完成，主流程快速返回。
2. **不丢消息**：生产端开启 Confirm，消费端关闭自动 ACK，业务处理成功后再 `basicAck`。
3. **不重扣库存**：Redis `SETNX` 做第一层幂等，`uk_order_no` 唯一索引做数据库层兜底。
4. **不超卖**：库存更新依赖 `version` 乐观锁，并发失败可重试或转入失败状态。

## 🚀 快速开始

### 环境要求

| 依赖 | 版本 | 默认连接 |
| --- | --- | --- |
| JDK | 21 | — |
| Maven | 3.9+（或直接用 `./mvnw`） | — |
| MySQL | 8.0 | `localhost:3306` |
| Redis | 6.0+ | `localhost:6379` |
| RabbitMQ | 3.x | `localhost:5672` / 管理台 `15672` |

macOS 下可用 Homebrew 一键安装并启动：

```bash
brew install openjdk@21 mysql redis rabbitmq
brew services start mysql redis rabbitmq
```

### 1. 初始化数据库

建库并创建 7 张表（RBAC 五表 + 物资表 + 订单表）：

```bash
mysql -uroot -h127.0.0.1 < src/main/resources/db/init.sql
```

### 2. 创建本地测试账号

`init.sql` 只负责建表，不包含种子数据。登录前先插入一个测试账号（账号 `admin` / 密码 `123456`）：

```sql
USE logiflow_db;

-- 密码为 123456 的 BCrypt 密文，仅用于本地开发，请勿在生产环境使用
INSERT INTO sys_user (username, password, nickname, status)
VALUES ('admin', '$2y$10$hBFLflrcjFhzHdtvfb5xMuvypW0x.Q8JpHaPcisz/3AyLaOBdZ3iK', '管理员', 1);
```

> 未分配角色和权限时，`UserDetailsServiceImpl` 会回退到一个默认权限标识，方便本地联调；后续可自行插入 `sys_role` / `sys_permission` 等数据体验完整的 RBAC。

### 3. 检查配置

默认配置位于 `src/main/resources/application.yml`，按需修改：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/logiflow_db...` | 数据库地址 |
| `spring.datasource.username` / `password` | `root` / 空 | 本机 MySQL 账号 |
| `spring.data.redis.host` / `port` | `localhost` / `6379` | Redis 连接 |
| `spring.rabbitmq.*` | `localhost:5672`，`guest/guest` | RabbitMQ 连接 |
| `jwt.secret` | 已提供开发密钥 | **HS256 要求至少 32 字节，部署前务必替换** |
| `jwt.expiration` | `604800000` | Token 有效期，默认 7 天 |

### 4. 启动服务

```bash
./mvnw spring-boot:run
```

服务默认监听 `http://localhost:8080`。首次启动前请确保 MySQL、Redis、RabbitMQ 均已运行，否则 Spring 上下文初始化会因连接失败而退出。

### 5. 验证登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}'
```

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenHead": "Bearer "
  }
}
```

后续请求在 `Authorization` 头中携带 Token：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 常用命令

```bash
./mvnw compile   # 编译
./mvnw test      # 运行测试（需三个中间件在线）
./mvnw package   # 打包到 target/logiflow-0.0.1-SNAPSHOT.jar
```

## 📡 接口与响应

### 已实现接口

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | 账号密码登录，返回 JWT | 否 |
| `GET` | `/test/hello?name=LogiFlow` | 连通性测试 | 否 |

除上述白名单外，其余请求均需通过 JWT 认证（见 `SecurityConfig`）。

### 统一响应体

所有接口返回同一种结构，由 `Result<T>` 封装：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| code | 含义 |
| --- | --- |
| `200` | 操作成功 |
| `400` | 参数校验失败 |
| `401` | 未登录或 Token 已过期 |
| `403` | 无相关权限 |
| `500` | 系统内部异常 |
| 自定义 | 业务异常可携带业务码，如 `4001` |

错误统一由 `GlobalExceptionHandler` 捕获：业务异常、`@Valid` 参数校验失败、系统未知异常各走各的处理分支，不会把堆栈直接暴露给前端。

## 🔐 认证与权限

```mermaid
flowchart LR
    A["POST /api/auth/login"] --> B["AuthenticationManager"]
    B --> C["UserDetailsServiceImpl<br/>查用户 + RBAC 权限"]
    C --> D["BCrypt 校验密码"]
    D --> E["JwtUtils 签发 Token<br/>HS256 / 7 天"]
    E --> F["返回 token + tokenHead"]
    F --> G["后续请求<br/>Authorization: Bearer ..."]
    G --> H["JwtAuthenticationFilter<br/>验签 + 校验过期"]
    H --> I["SecurityContext<br/>写入认证信息"]
```

- 密码使用 **BCrypt** 加盐哈希存储，数据库中不保存明文。
- Token 为无状态 JWT，服务端不保存会话；`sessionCreationPolicy(STATELESS)`。
- 权限标识采用 `sys:order:add` 这类格式，由 RBAC 五表 JOIN 聚合到 `UserDetails`，可直接配合方法级注解使用：

```java
// 规划中的下单接口：权限注解的实际用法
@PreAuthorize("hasAuthority('sys:order:add')")
@PostMapping("/api/orders")
public Result<String> createOrder(@RequestBody @Valid OrderCreateDTO dto) {
  // ...
}
```

## 🗄️ 数据库设计

```mermaid
erDiagram
    SYS_USER ||--o{ SYS_USER_ROLE : "拥有"
    SYS_ROLE ||--o{ SYS_USER_ROLE : "被分配"
    SYS_ROLE ||--o{ SYS_ROLE_PERMISSION : "授予"
    SYS_PERMISSION ||--o{ SYS_ROLE_PERMISSION : "包含"
    SYS_USER ||--o{ BIZ_ORDER : "申领"
    BIZ_MATERIAL ||--o{ BIZ_ORDER : "出库"
```

| 表名 | 说明 | 关键设计 |
| --- | --- | --- |
| `sys_user` | 系统用户 | `uk_username` 账号唯一；密码存 BCrypt 密文 |
| `sys_role` | 角色 | `uk_role_code`，如 `ROLE_ADMIN` |
| `sys_permission` | 权限 / 菜单 | `uk_perm_code`；`parent_id` 组成树形菜单 |
| `sys_user_role` | 用户-角色关联 | 联合主键 |
| `sys_role_permission` | 角色-权限关联 | 联合主键 |
| `biz_material` | 物资库存 | `uk_material_code`；`version` 乐观锁防超卖 |
| `biz_order` | 申领订单 | `uk_order_no` 是 MQ 消费幂等的数据库兜底 |

订单状态机：

```mermaid
stateDiagram-v2
    [*] --> Pending: 创建申领单
    Pending --> Completed: 库存扣减成功
    Pending --> Failed: 库存不足
    Pending --> Closed: 超时关闭（规划）
    Completed --> [*]
    Failed --> [*]
    Closed --> [*]
```

| 状态值 | 含义 |
| --- | --- |
| `0` | 排队处理中 |
| `1` | 已完成（已扣库存） |
| `2` | 库存不足失败 |
| `3` | 超时关闭 |

## 📁 项目结构

```text
logiflow-server
├── src/main/java/com/rickrone/logiflow
│   ├── common
│   │   ├── api          # Result / ResultCode 统一响应契约
│   │   ├── exception    # BusinessException + 全局异常处理
│   │   └── utils        # JwtUtils 签发 / 解析 / 校验
│   ├── config           # SecurityConfig 安全过滤链
│   ├── controller       # AuthController 登录、TestController 联调
│   ├── security         # JWT 过滤器、UserDetailsServiceImpl
│   ├── mapper           # MyBatis-Plus Mapper
│   └── model
│       ├── dto          # LoginDTO 等入参对象
│       └── entity       # SysUser / BizMaterial / BizOrder
├── src/main/resources
│   ├── application.yml  # 数据源、Redis、RabbitMQ、JWT 配置
│   └── db/init.sql      # 建库 + 7 张表
├── docs/BLUEPRINT.md    # 项目蓝图与实施路线
├── pom.xml
└── mvnw / mvnw.cmd
```

## 🗺️ 路线图

### ✅ Phase 1 · 工程地基与认证（已完成）

- [x] Spring Boot 4 + MyBatis-Plus 工程骨架
- [x] 统一响应体 `Result<T>` 与错误码枚举 `ResultCode`
- [x] 全局异常处理（业务异常 / 参数校验 / 系统兜底）
- [x] Spring Security 无状态过滤链 + JWT 登录
- [x] BCrypt 密码加密与 RBAC 五表权限模型
- [x] 用户、物资、订单实体与基础 Mapper
- [x] 数据库初始化脚本

### 🚧 Phase 2 · 物资与订单 CRUD（进行中）

- [ ] 物资管理：分页查询、新增、编辑、逻辑删除
- [ ] DTO / VO 严格分层与 `@Valid` 入参校验
- [ ] 申领订单创建接口（同步落单）
- [ ] 参数校验与业务异常场景补全

### 📋 Phase 3 · 异步下单与高可用

- [ ] RabbitMQ 交换机 / 队列 / 绑定声明
- [ ] 生产端 Confirm 与消费者手动 ACK
- [ ] Redis `SETNX` + 唯一索引双重幂等
- [ ] 乐观锁扣库存与失败状态落库
- [ ] 延时队列实现超时关单

### 📋 Phase 4 · 容器化与前端联调

- [ ] 后端多阶段构建 Dockerfile
- [ ] Docker Compose 编排 MySQL / Redis / RabbitMQ / Backend
- [ ] Vue 3 管理后台：登录、物资、订单页面
- [ ] Nginx 部署前端静态资源

## 🤝 贡献

欢迎 Issue 和 Pull Request。提交前请确保：

```bash
./mvnw compile && ./mvnw test
```

建议从 `dev` 分支切出功能分支，提交信息遵循 `feat: / fix: / docs:` 前缀。

## 📄 License

本项目基于 [MIT License](LICENSE) 开源。

---

<div align="center">
  <sub>Built with ☕ and Spring Boot · 如果这个项目对你有帮助，欢迎点一个 ⭐</sub>
</div>
