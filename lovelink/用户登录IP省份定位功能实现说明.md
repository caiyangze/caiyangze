# 用户登录IP省份定位功能实现说明

## 功能概述

实现了用户登录时自动获取IP地址并转换为省份信息的功能，同时支持用户手动修改省份。该功能包括：

1. **登录时自动获取IP省份**：用户登录时自动获取客户端IP并通过高德地图API转换为省份
2. **前端省份显示**：在首页和相亲广场显示当前登录省份
3. **手动省份选择**：用户可以通过选择器手动修改省份
4. **省份数据持久化**：省份信息保存到用户表的 `last_login_ip` 字段

## 后端实现

### 1. IP定位服务 (AmapIpLocation.java)

**位置**: `lovelink-social/src/main/java/com/zhentao/config/AmapIpLocation.java`

**主要功能**:
- 调用高德地图IP定位API
- 将IP地址转换为省份信息
- 支持自动获取请求IP或指定IP

**核心方法**:
```java
@Service
public class AmapIpLocation {
    public String getProvinceByIp(String ip) {
        // 调用高德API获取省份信息
    }
}
```

### 2. IP工具类 (IpUtils.java)

**位置**: `lovelink-common/src/main/java/com/zhentao/utils/IpUtils.java`

**主要功能**:
- 获取客户端真实IP地址
- 处理代理服务器转发的IP
- 判断内网IP和外网IP

**核心方法**:
```java
public class IpUtils {
    public static String getClientIp(HttpServletRequest request)
    public static boolean isInternalIp(String ip)
}
```

### 3. 用户控制器修改 (UserController.java)

**位置**: `lovelink-user/src/main/java/com/zhentao/controller/UserController.java`

**新增功能**:
- 登录接口自动更新IP省份
- 省份手动更新接口
- 登录信息更新方法

**新增接口**:
```java
@PostMapping("/updateProvince")
public Result updateProvince(@RequestBody Map<String, String> requestBody)

private void updateLoginInfo(TbUser user, HttpServletRequest request)
```

### 4. 数据库字段

**表**: `tb_user`
**字段**: `last_login_ip` (varchar(255))
**用途**: 存储用户最后登录的省份信息

## 前端实现

### 1. 省份常量文件

**位置**: `abab_uniapp/utils/provinces.js`

**内容**:
- 中国34个省份列表
- 省份简称映射
- 省份名称转换工具方法

### 2. API接口

**位置**: `abab_uniapp/api/user/auth.js`

**新增接口**:
```javascript
// 更新用户省份
export const updateUserProvince = (token, province) => {
  return http.post(`${BASE_PATH}/updateProvince`, { token, province })
}
```

### 3. 页面修改

#### 首页 (pages/index/index.vue)
- 地区选择器改为34个省份
- 页面加载时获取用户当前省份
- 省份选择后自动更新到后端

#### 相亲广场 (pages/square/square.vue)
- 同首页的省份选择功能
- 保持界面一致性

**核心功能**:
```javascript
// 获取用户信息并设置当前省份
async function loadUserInfo() {
  const result = await getByUserInfo(token);
  if (result.data.lastLoginIp) {
    selectedRegion.value = getProvinceShortName(result.data.lastLoginIp);
  }
}

// 更新用户省份信息
async function updateUserProvinceInfo(province) {
  const result = await updateUserProvince(token, province);
  // 处理更新结果
}
```

## 功能流程

### 1. 用户登录流程

```
用户登录 → 获取客户端IP → 调用高德API → 获取省份 → 更新数据库 → 返回登录结果
```

### 2. 省份显示流程

```
页面加载 → 获取用户信息 → 读取省份字段 → 显示在地区选择器
```

### 3. 手动修改流程

```
用户选择省份 → 调用更新接口 → 更新数据库 → 显示成功提示
```

## 技术特点

### 1. 智能IP识别
- 支持代理服务器环境
- 自动过滤内网IP
- 处理IPv4和IPv6地址

### 2. 容错机制
- IP定位失败不影响登录
- 网络异常时使用默认省份
- 详细的错误日志记录

### 3. 用户体验
- 自动获取省份，无需用户操作
- 支持手动修改，满足个性化需求
- 省份选择器包含全国34个省份

### 4. 数据一致性
- 登录时实时更新
- 前后端数据同步
- 省份信息持久化存储

## 配置说明

### 1. 高德地图API配置

**API Key**: `da0db48121bc669e9d9e81bf5d1940c0`
**接口地址**: `https://restapi.amap.com/v3/ip`

### 2. 数据库配置

确保 `tb_user` 表包含 `last_login_ip` 字段：
```sql
ALTER TABLE tb_user ADD COLUMN last_login_ip VARCHAR(255) COMMENT '最后登录省份';
```

## 测试验证

### 1. 后端测试

运行测试类验证IP定位功能：
```bash
mvn test -Dtest=AmapIpLocationTest
```

### 2. 前端测试

1. 用户登录后检查省份显示
2. 手动修改省份验证更新功能
3. 重新登录验证省份持久化

## 注意事项

1. **API限制**: 高德地图API有调用频率限制，生产环境需要注意
2. **内网环境**: 内网IP无法获取真实地理位置，会跳过定位
3. **数据兼容**: 老用户的 `last_login_ip` 字段可能为空，需要兼容处理
4. **错误处理**: IP定位失败不应影响正常登录流程

## 后续优化建议

1. **缓存机制**: 对相同IP的定位结果进行缓存
2. **批量更新**: 支持批量更新用户省份信息
3. **统计分析**: 基于省份数据进行用户分布统计
4. **精确定位**: 考虑支持城市级别的定位
