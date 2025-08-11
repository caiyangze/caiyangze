# 多阶段构建 Dockerfile for lovelink project
# 第一阶段：构建阶段
FROM maven:3.8.4-openjdk-8 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件
COPY pom.xml .
COPY lovelink-common/pom.xml lovelink-common/
COPY lovelink-user/pom.xml lovelink-user/
COPY lovelink-social/pom.xml lovelink-social/
COPY lovelink-matchmaker/pom.xml lovelink-matchmaker/

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY lovelink-common/src lovelink-common/src
COPY lovelink-user/src lovelink-user/src
COPY lovelink-social/src lovelink-social/src
COPY lovelink-matchmaker/src lovelink-matchmaker/src

# 构建项目
RUN mvn clean package -DskipTests -B

# 第二阶段：运行阶段
FROM openjdk:8-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl

# 创建应用用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# 从构建阶段复制jar文件
COPY --from=builder /app/lovelink-user/target/*.jar app-user.jar
COPY --from=builder /app/lovelink-social/target/*.jar app-social.jar
COPY --from=builder /app/lovelink-matchmaker/target/*.jar app-matchmaker.jar

# 修改文件权限
RUN chown -R appuser:appgroup /app

# 切换到应用用户
USER appuser

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 暴露端口（将在docker-compose中具体指定）
EXPOSE 9001 9002 9004

# 启动脚本将在docker-compose中指定
