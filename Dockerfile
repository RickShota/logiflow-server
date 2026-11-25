# syntax=docker/dockerfile:1

# 第一阶段：使用 Maven 构建打包
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 先拷贝 pom.xml 下载依赖，利用 Docker 层缓存加速后续构建
COPY pom.xml .
# BuildKit 缓存挂载：Maven 本地仓库跨构建复用，改代码后不必重新下载依赖
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

# 拷贝源码并打包（跳过测试编译与执行）
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -Dmaven.test.skip=true

# 第二阶段：精简运行时镜像
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 时区 + JVM 容器内存参数（MaxRAMPercentage 让堆随容器内存自适应）
ENV TZ=Asia/Shanghai \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从构建阶段拷贝可执行 jar（*.jar 不会匹配到 *.jar.original）
COPY --from=builder /app/target/*.jar app.jar

# 非 root 用户运行：提前创建日志目录并授权
RUN groupadd --system app && useradd --system --gid app --home-dir /app app \
    && mkdir -p /app/logs && chown -R app:app /app
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
