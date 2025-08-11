# MinIO配置问题解决方案

## 问题描述

启动social模块时出现以下错误：
```
java.lang.IllegalArgumentException: endpoint must not be null.
```

这是因为MinIO配置中的endpoint为null导致的。

## 问题原因

1. **Nacos配置中心连接问题**：项目使用Nacos作为配置中心，但可能无法连接到Nacos服务器
2. **缺少本地配置**：当Nacos不可用时，没有本地的MinIO配置作为备用
3. **配置优先级问题**：bootstrap.yml中的Nacos配置优先级高于application.yml

## 解决方案

### 方案1：禁用Nacos，使用本地配置（推荐用于开发测试）

我已经为以下模块添加了本地MinIO配置：

1. **lovelink-social/src/main/resources/application.yml**
2. **lovelink-content/src/main/resources/application.yml**
3. **lovelink-user/src/main/resources/application.yml**（已存在）

并在bootstrap.yml中暂时禁用了Nacos：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: false  # 暂时禁用Nacos发现
      config:
        enabled: false  # 暂时禁用Nacos配置中心
```

### 方案2：配置Nacos中的MinIO配置

如果要使用Nacos配置中心，需要在Nacos中添加MinIO配置：

1. 登录Nacos控制台：http://182.254.244.209:8848/nacos
2. 在lovelink命名空间下创建配置文件
3. 配置内容：
```yaml
minio:
  endpoint: http://182.254.244.209:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: lovelink
```

### 方案3：本地MinIO服务

如果使用本地MinIO服务，需要：

1. 启动本地MinIO服务（端口9000）
2. 使用默认配置：
```yaml
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: lovelink
```

## 当前配置状态

### 已配置的模块

- ✅ **lovelink-user**: 已有完整的MinIO配置
- ✅ **lovelink-social**: 已添加本地MinIO配置
- ✅ **lovelink-content**: 已添加本地MinIO配置

### 配置内容

```yaml
# MinIO配置
minio:
  endpoint: http://localhost:9000  # 本地MinIO服务
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: lovelink

# 数据库配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/lovelink?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

## 启动测试

### 启动User模块（聊天功能）
```bash
cd lovelink-user
mvn spring-boot:run
```

### 启动Social模块
```bash
cd lovelink-social
mvn spring-boot:run
```

### 测试聊天功能
访问：http://localhost:9001/chat-test.html

## 注意事项

1. **数据库连接**：确保MySQL数据库可用，并且已创建lovelink数据库
2. **MinIO服务**：如果使用本地MinIO，确保服务已启动
3. **端口冲突**：确保9001、9002等端口未被占用
4. **依赖安装**：确保Maven依赖已正确下载

## 恢复Nacos配置

如果后续需要恢复Nacos配置，将bootstrap.yml中的enabled改为true：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
      config:
        enabled: true
```
