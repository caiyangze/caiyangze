#!/bin/bash

# 聊巴项目 Docker 启动脚本
# 作者: zhentao
# 日期: $(date +%Y-%m-%d)

echo "=========================================="
echo "      聊巴项目 Docker 容器启动脚本"
echo "=========================================="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 创建必要的目录
echo "📁 创建必要的目录..."
mkdir -p docker/mysql/conf
mkdir -p docker/mysql/init
mkdir -p docker/redis
mkdir -p logs

# 检查配置文件是否存在
echo "🔍 检查配置文件..."
config_files=(
    "docker/mysql/conf/my.cnf"
    "docker/mysql/init/01-init.sql"
    "docker/redis/redis.conf"
    "docker-compose.yml"
)

for file in "${config_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ 配置文件 $file 不存在"
        exit 1
    fi
done

echo "✅ 配置文件检查完成"

# 构建项目
echo "🔨 构建 Maven 项目..."
if ! mvn clean package -DskipTests; then
    echo "❌ Maven 构建失败"
    exit 1
fi

echo "✅ Maven 构建完成"

# 停止并删除现有容器
echo "🛑 停止现有容器..."
docker-compose down

# 清理悬空镜像
echo "🧹 清理悬空镜像..."
docker image prune -f

# 启动服务
echo "🚀 启动 Docker 服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose ps

# 显示服务访问地址
echo ""
echo "=========================================="
echo "           服务访问地址"
echo "=========================================="
echo "🌐 用户服务:        http://localhost:9001"
echo "🌐 社交服务:        http://localhost:9002"
echo "🌐 红娘服务:        http://localhost:9004"
echo "🌐 Nacos控制台:     http://localhost:8848/nacos"
echo "🌐 MinIO控制台:     http://localhost:9001"
echo "🌐 MySQL数据库:     localhost:3306"
echo "🌐 Redis缓存:       localhost:6379"
echo ""
echo "📝 默认账号信息:"
echo "   Nacos: nacos/nacos"
echo "   MinIO: minioadmin/minioadmin"
echo "   MySQL: lovelink/lovelink123"
echo ""
echo "=========================================="
echo "           启动完成"
echo "=========================================="

# 显示日志查看命令
echo "📋 常用命令:"
echo "   查看所有服务日志: docker-compose logs -f"
echo "   查看用户服务日志: docker-compose logs -f lovelink-user"
echo "   查看社交服务日志: docker-compose logs -f lovelink-social"
echo "   查看红娘服务日志: docker-compose logs -f lovelink-matchmaker"
echo "   停止所有服务:     docker-compose down"
echo "   重启所有服务:     docker-compose restart"
