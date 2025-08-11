# Dockerfile for lovelink-matchmaker service
FROM openjdk:8-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl tzdata

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# 复制jar文件
COPY target/*.jar app.jar

# 修改文件权限
RUN chown -R appuser:appgroup /app

# 切换到应用用户
USER appuser

# 设置JVM参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:9004/actuator/health || exit 1

# 暴露端口
EXPOSE 9004

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
