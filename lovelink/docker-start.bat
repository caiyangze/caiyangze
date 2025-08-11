@echo off
chcp 65001 >nul
title 聊巴项目 Docker 启动脚本

echo ==========================================
echo       聊巴项目 Docker 容器启动脚本
echo ==========================================

REM 检查 Docker 是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker 未安装，请先安装 Docker Desktop
    pause
    exit /b 1
)

REM 检查 Docker Compose 是否安装
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose 未安装，请先安装 Docker Compose
    pause
    exit /b 1
)

REM 创建必要的目录
echo 📁 创建必要的目录...
if not exist "docker\mysql\conf" mkdir "docker\mysql\conf"
if not exist "docker\mysql\init" mkdir "docker\mysql\init"
if not exist "docker\redis" mkdir "docker\redis"
if not exist "logs" mkdir "logs"

REM 检查配置文件是否存在
echo 🔍 检查配置文件...
if not exist "docker\mysql\conf\my.cnf" (
    echo ❌ 配置文件 docker\mysql\conf\my.cnf 不存在
    pause
    exit /b 1
)

if not exist "docker\mysql\init\01-init.sql" (
    echo ❌ 配置文件 docker\mysql\init\01-init.sql 不存在
    pause
    exit /b 1
)

if not exist "docker\redis\redis.conf" (
    echo ❌ 配置文件 docker\redis\redis.conf 不存在
    pause
    exit /b 1
)

if not exist "docker-compose.yml" (
    echo ❌ 配置文件 docker-compose.yml 不存在
    pause
    exit /b 1
)

echo ✅ 配置文件检查完成

REM 构建项目
echo 🔨 构建 Maven 项目...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ Maven 构建失败
    pause
    exit /b 1
)

echo ✅ Maven 构建完成

REM 停止并删除现有容器
echo 🛑 停止现有容器...
docker-compose down

REM 清理悬空镜像
echo 🧹 清理悬空镜像...
docker image prune -f

REM 启动服务
echo 🚀 启动 Docker 服务...
docker-compose up -d

REM 等待服务启动
echo ⏳ 等待服务启动...
timeout /t 30 /nobreak >nul

REM 检查服务状态
echo 📊 检查服务状态...
docker-compose ps

REM 显示服务访问地址
echo.
echo ==========================================
echo            服务访问地址
echo ==========================================
echo 🌐 用户服务:        http://localhost:9001
echo 🌐 社交服务:        http://localhost:9002
echo 🌐 红娘服务:        http://localhost:9004
echo 🌐 Nacos控制台:     http://localhost:8848/nacos
echo 🌐 MinIO控制台:     http://localhost:9001
echo 🌐 MySQL数据库:     localhost:3306
echo 🌐 Redis缓存:       localhost:6379
echo.
echo 📝 默认账号信息:
echo    Nacos: nacos/nacos
echo    MinIO: minioadmin/minioadmin
echo    MySQL: lovelink/lovelink123
echo.
echo ==========================================
echo            启动完成
echo ==========================================

REM 显示日志查看命令
echo 📋 常用命令:
echo    查看所有服务日志: docker-compose logs -f
echo    查看用户服务日志: docker-compose logs -f lovelink-user
echo    查看社交服务日志: docker-compose logs -f lovelink-social
echo    查看红娘服务日志: docker-compose logs -f lovelink-matchmaker
echo    停止所有服务:     docker-compose down
echo    重启所有服务:     docker-compose restart

pause
