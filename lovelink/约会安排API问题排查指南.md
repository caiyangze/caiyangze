# 约会安排API问题排查指南

## 🚨 问题描述

前端调用约会安排API时出现405错误：
```
GET http://localhost:9004/matchmaker/date/available-requests?pageNum=1&pageSize=20 405 (Method Not Allowed)
```

## 🔧 已修复的问题

### 1. 控制器路径映射修复

**问题**：控制器的`@RequestMapping`路径不匹配

**修复前**：
```java
@RestController
@RequestMapping("/date")  // 错误路径
public class DateArrangementController {
```

**修复后**：
```java
@RestController
@RequestMapping("/matchmaker/date")  // 正确路径
public class DateArrangementController {
```

### 2. 添加测试接口

为了验证服务是否正常运行，添加了测试接口：
```java
@GetMapping("/test")
public Result test() {
    return Result.OK("约会安排服务正常运行");
}
```

## 🧪 测试步骤

### 步骤1：测试服务是否运行

访问测试接口：
```
GET http://localhost:9004/matchmaker/date/test
```

**预期结果**：
```json
{
  "code": 200,
  "message": "success",
  "data": "约会安排服务正常运行"
}
```

### 步骤2：测试获取可安排约会列表

使用红娘账号的token访问：
```
GET http://localhost:9004/matchmaker/date/available-requests?pageNum=1&pageSize=20
Headers: token: [红娘的JWT token]
```

**预期结果**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 0,
    "current": 1,
    "size": 20,
    "pages": 0
  }
}
```

### 步骤3：测试安排约会接口

```
POST http://localhost:9004/matchmaker/date/arrange
Headers: 
  Content-Type: application/json
  token: [红娘的JWT token]
Body:
{
  "requestId": 1,
  "dateTime": "2025-01-15T19:00:00.000Z",
  "dateLocation": "星巴克咖啡（万达广场店）",
  "dateType": 1,
  "datePlan": "建议先在咖啡厅聊天30分钟"
}
```

## 🔍 可能的其他问题

### 1. 服务未启动

**检查方法**：
```bash
# 检查9004端口是否被占用
netstat -an | grep 9004

# 或者使用lsof（Linux/Mac）
lsof -i :9004
```

**解决方案**：
```bash
cd lovelink-matchmaker
mvn spring-boot:run
```

### 2. 数据库连接问题

**检查日志**：查看控制台是否有数据库连接错误

**解决方案**：
- 确认数据库服务正在运行
- 检查数据库连接配置
- 验证数据库表是否存在

### 3. 依赖注入问题

**可能的错误**：
- `TbDateArrangementService` 未正确注入
- `TbMatchmakingRequestService` 未正确注入
- `TbUserService` 未正确注入

**解决方案**：
- 检查Service类是否有`@Service`注解
- 检查Mapper接口是否有`@Mapper`注解
- 确认包扫描路径正确

### 4. JWT验证问题

**可能的错误**：
- Token格式不正确
- Token已过期
- JwtService配置问题

**解决方案**：
- 检查前端发送的token格式
- 验证token是否有效
- 确认JwtService配置正确

## 📋 完整的API列表

### 约会安排相关API

| 方法 | 路径 | 功能 | 参数 |
|------|------|------|------|
| GET | `/matchmaker/date/test` | 测试服务 | 无 |
| GET | `/matchmaker/date/available-requests` | 获取可安排约会的申请 | pageNum, pageSize |
| POST | `/matchmaker/date/arrange` | 安排约会 | DateArrangementDTO |
| GET | `/matchmaker/date/list` | 获取约会列表 | pageNum, pageSize, arrangementStatus |

### 请求头要求

所有API（除了test）都需要在请求头中包含：
```
token: [有效的JWT token]
Content-Type: application/json
```

## 🚀 重启服务

如果问题仍然存在，请重启后端服务：

```bash
# 停止当前服务（如果正在运行）
# Ctrl+C 或者 kill 进程

# 重新启动服务
cd lovelink-matchmaker
mvn clean compile
mvn spring-boot:run
```

## 📞 进一步排查

如果以上步骤都无法解决问题，请检查：

1. **控制台日志**：查看详细的错误信息
2. **网络连接**：确认前后端网络连通性
3. **端口冲突**：确认9004端口没有被其他服务占用
4. **配置文件**：检查application.yml配置是否正确

## ✅ 验证修复

修复后，前端应该能够：
1. 成功加载待安排约会列表
2. 正常显示约会安排表单
3. 成功提交约会安排
4. 查看已安排约会列表

如果仍有问题，请提供详细的错误日志以便进一步排查。
