# VIP功能Token方式实现说明

## 🔄 用户ID获取方式统一

根据您的要求，已将VIP控制器中的用户ID获取方式统一为：

```java
@RequestHeader("token") String token
Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
Integer userId = (Integer) claimsMap.get("userId");
```

## 📝 修改详情

### 1. 导入JwtService
```java
import com.zhentao.utils.JwtService;
```

### 2. 修改所有接口方法签名

#### 创建VIP订单
```java
@PostMapping("/createOrder")
public Result createVipOrder(@RequestBody Map<String, Object> requestData, 
                            @RequestHeader("token") String token)
```

#### 处理VIP支付
```java
@PostMapping("/pay")
@Transactional(rollbackFor = Exception.class)
public Result processVipPayment(@RequestBody Map<String, Object> requestData,
                               @RequestHeader("token") String token)
```

#### 获取VIP状态
```java
@GetMapping("/status")
public Result getUserVipStatus(@RequestHeader("token") String token)
```

#### 查询订单详情
```java
@GetMapping("/order/{orderId}")
public Result getVipOrderDetail(@PathVariable Long orderId, 
                               @RequestHeader("token") String token)
```

### 3. 添加统一的用户ID获取方法

```java
/**
 * 从token中获取用户ID
 *
 * @param token JWT token
 * @return 用户ID，如果获取失败返回null
 */
private Long getUserIdFromToken(String token) {
    try {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        if (claimsMap != null && claimsMap.containsKey("userId")) {
            Object userIdObj = claimsMap.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.valueOf((String) userIdObj);
            }
        }
    } catch (Exception e) {
        log.warn("从token获取用户ID失败: {}", e.getMessage());
    }
    return null;
}
```

### 4. 替换所有用户ID获取调用

**之前**:
```java
Long userId = ThreadLocalUtil.getCurrentUserId();
```

**现在**:
```java
Long userId = getUserIdFromToken(token);
```

## 🧪 API接口测试

### 1. 创建VIP订单
```http
POST /VIP/createOrder
Content-Type: application/json
token: eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEwMDF9...

{
    "vipType": 2
}
```

### 2. 处理VIP支付
```http
POST /VIP/pay
Content-Type: application/json
token: eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEwMDF9...

{
    "orderId": 123,
    "payType": 1
}
```

### 3. 查询VIP状态
```http
GET /VIP/status
token: eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEwMDF9...
```

### 4. 查询订单详情
```http
GET /VIP/order/123
token: eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEwMDF9...
```

## 🔧 类型兼容性处理

考虑到JWT中的userId可能是不同类型（Integer、Long、String），添加了完整的类型转换处理：

```java
Object userIdObj = claimsMap.get("userId");
if (userIdObj instanceof Integer) {
    return ((Integer) userIdObj).longValue();
} else if (userIdObj instanceof Long) {
    return (Long) userIdObj;
} else if (userIdObj instanceof String) {
    return Long.valueOf((String) userIdObj);
}
```

## 🎯 核心功能保持不变

用户ID获取方式的修改不影响核心业务逻辑：

✅ **tb_vip_order表插入数据** - 订单创建和状态更新
✅ **用户角色字段变为VIP (userRole = 2)** - 支付成功后自动更新  
✅ **tb_wallet_transaction表交易记录** - 完整的交易流水记录

## 📱 前端调用方式

前端调用时需要在请求头中包含token：

```javascript
// 示例：创建VIP订单
const response = await uni.request({
  url: 'http://your-api-domain/VIP/createOrder',
  method: 'POST',
  header: {
    'Content-Type': 'application/json',
    'token': uni.getStorageSync('token') // 从本地存储获取token
  },
  data: {
    vipType: 2
  }
});
```

## 🔍 调试建议

如果遇到用户ID获取失败的问题，可以：

1. **检查token格式**：确保token是有效的JWT格式
2. **验证token内容**：使用JWT解析工具查看token中是否包含userId字段
3. **检查token有效性**：确保token未过期
4. **查看日志**：控制器中已添加警告日志，可以查看具体错误信息

## 🚀 部署注意事项

1. **确保JwtService可用**：项目中的JwtService类需要正确配置
2. **token传递**：前端需要在所有VIP相关请求中包含token头
3. **异常处理**：token解析失败时会返回"用户未登录"错误

## 📋 测试清单

- [ ] 创建VIP订单接口测试
- [ ] VIP支付处理接口测试  
- [ ] VIP状态查询接口测试
- [ ] 订单详情查询接口测试
- [ ] token无效时的错误处理测试
- [ ] 数据库记录验证（三个表的数据一致性）

现在VIP功能已经完全适配您的token获取方式，保持了与项目其他部分的一致性！🎯
