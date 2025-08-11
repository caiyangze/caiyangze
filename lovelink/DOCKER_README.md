# 聊巴项目 Docker 部署指南

## 📋 项目概述

聊巴是一个基于 Spring Boot 微服务架构的社交婚恋平台，包含用户服务、社交服务和红娘服务三个核心模块。

## 🏗️ 架构说明

### 服务架构
```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   用户服务      │  │   社交服务      │  │   红娘服务      │
│  (Port: 9001)   │  │  (Port: 9002)   │  │  (Port: 9004)   │
│                 │  │                 │  │                 │
│ • 用户管理      │  │ • 动态发布      │  │ • 红娘申请      │
│ • 聊天功能      │  │ • 社交互动      │  │ • 匹配服务      │
│ • 钱包系统      │  │ • 内容管理      │  │ • 约会安排      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│     MySQL       │  │     Redis       │  │     MinIO       │
│  (Port: 3306)   │  │  (Port: 6379)   │  │  (Port: 9000)   │
│                 │  │                 │  │                 │
│ • 业务数据      │  │ • 缓存数据      │  │ • 文件存储      │
│ • Nacos配置     │  │ • 会话管理      │  │ • 图片视频      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 技术栈
- **后端框架**: Spring Boot 2.3.2 + Spring Cloud Hoxton.SR8
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.2
- **对象存储**: MinIO
- **服务发现**: Nacos 2.1.0
- **ORM框架**: MyBatis Plus 3.5.2
- **容器化**: Docker + Docker Compose

## 🚀 快速开始

### 前置要求
- Docker 20.10+
- Docker Compose 1.29+
- Maven 3.6+
- JDK 1.8+

### 1. 克隆项目
```bash
git clone <your-repository-url>
cd lovelink
```

### 2. 一键启动（推荐）

**Linux/macOS:**
```bash
chmod +x docker-start.sh
./docker-start.sh
```

**Windows:**
```cmd
docker-start.bat
```

### 3. 手动启动

#### 3.1 构建项目
```bash
mvn clean package -DskipTests
```

#### 3.2 启动容器
```bash
docker-compose up -d
```

#### 3.3 查看服务状态
```bash
docker-compose ps
```

## 🌐 服务访问地址

| 服务名称 | 访问地址 | 说明 |
|---------|---------|------|
| 用户服务 | http://localhost:9001 | 用户管理、聊天功能 |
| 社交服务 | http://localhost:9002 | 动态发布、社交互动 |
| 红娘服务 | http://localhost:9004 | 红娘申请、匹配服务 |
| Nacos控制台 | http://localhost:8848/nacos | 服务注册与配置管理 |
| MinIO控制台 | http://localhost:9001 | 对象存储管理 |
| MySQL数据库 | localhost:3306 | 数据库服务 |
| Redis缓存 | localhost:6379 | 缓存服务 |

## 🔐 默认账号信息

| 服务 | 用户名 | 密码 |
|------|--------|------|
| Nacos | nacos | nacos |
| MinIO | minioadmin | minioadmin |
| MySQL | lovelink | lovelink123 |

## 📁 项目结构

```
lovelink/
├── docker-compose.yml              # Docker Compose 配置文件
├── Dockerfile                      # 通用 Dockerfile
├── docker-start.sh                 # Linux/macOS 启动脚本
├── docker-start.bat                # Windows 启动脚本
├── docker/                         # Docker 配置目录
│   ├── mysql/
│   │   ├── conf/my.cnf             # MySQL 配置文件
│   │   └── init/01-init.sql        # 数据库初始化脚本
│   └── redis/
│       └── redis.conf              # Redis 配置文件
├── lovelink-common/                # 公共模块
├── lovelink-user/                  # 用户服务
│   ├── Dockerfile                  # 用户服务 Dockerfile
│   └── src/main/resources/
│       └── application-docker.yml  # Docker 环境配置
├── lovelink-social/                # 社交服务
│   ├── Dockerfile                  # 社交服务 Dockerfile
│   └── src/main/resources/
│       └── application-docker.yml  # Docker 环境配置
└── lovelink-matchmaker/            # 红娘服务
    ├── Dockerfile                  # 红娘服务 Dockerfile
    └── src/main/resources/
        └── application-docker.yml  # Docker 环境配置
```

## 🛠️ 常用命令

### 服务管理
```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启所有服务
docker-compose restart

# 重新构建并启动
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看服务资源使用情况
docker-compose top
```

### 日志查看
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f lovelink-user
docker-compose logs -f lovelink-social
docker-compose logs -f lovelink-matchmaker

# 查看最近100行日志
docker-compose logs --tail=100 lovelink-user
```

### 数据管理
```bash
# 备份 MySQL 数据
docker exec lovelink-mysql mysqldump -u lovelink -plovelink123 lovelink > backup.sql

# 恢复 MySQL 数据
docker exec -i lovelink-mysql mysql -u lovelink -plovelink123 lovelink < backup.sql

# 清理 Redis 缓存
docker exec lovelink-redis redis-cli FLUSHALL
```

## 🔧 配置说明

### 环境变量配置
可以通过修改 `docker-compose.yml` 中的环境变量来调整配置：

```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/lovelink
  SPRING_REDIS_HOST: redis
  MINIO_ENDPOINT: http://minio:9000
```

### 资源限制
可以为每个服务设置资源限制：

```yaml
deploy:
  resources:
    limits:
      memory: 1G
      cpus: '0.5'
    reservations:
      memory: 512M
      cpus: '0.25'
```

## 🐛 故障排除

### 常见问题

1. **端口冲突**
   - 检查本地是否有服务占用相同端口
   - 修改 `docker-compose.yml` 中的端口映射

2. **内存不足**
   - 增加 Docker Desktop 的内存限制
   - 调整 JVM 参数

3. **服务启动失败**
   - 查看服务日志：`docker-compose logs <service-name>`
   - 检查配置文件是否正确

4. **数据库连接失败**
   - 确保 MySQL 服务已启动
   - 检查数据库连接配置

### 健康检查
所有服务都配置了健康检查，可以通过以下命令查看：

```bash
docker-compose ps
```

## 📊 监控和维护

### 性能监控
- 使用 `docker stats` 查看容器资源使用情况
- 通过 Spring Boot Actuator 端点监控应用状态

### 日志管理
- 日志文件自动轮转，保留30天
- 单个日志文件最大100MB

### 数据备份
建议定期备份以下数据：
- MySQL 数据库
- MinIO 存储文件
- 应用配置文件

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
