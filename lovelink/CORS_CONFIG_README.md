# 聊巴项目跨域配置说明

## 🌐 跨域问题解决方案

本项目已经为所有微服务配置了完整的跨域支持，解决前端与后端对接时的跨域问题。

## 📋 配置概述

### 1. 多层次跨域配置

我们采用了多层次的跨域配置策略，确保跨域请求能够正常处理：

- **CorsFilter 过滤器**：最高优先级的跨域处理
- **WebMvcConfigurer 全局配置**：Spring MVC 层面的跨域配置
- **@CrossOrigin 注解**：控制器级别的跨域配置
- **配置文件设置**：application.yml 中的跨域配置

### 2. 配置的服务

✅ **lovelink-user** (端口: 9001)
✅ **lovelink-social** (端口: 9002)  
✅ **lovelink-matchmaker** (端口: 9004)

## 🔧 配置详情

### CorsFilter 配置

每个服务都配置了 `CorsFilter`，位于 `config/CorsConfig.java`：

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有域名进行跨域调用
        config.addAllowedOrigin("*");
        config.setAllowCredentials(false); // 当使用 * 时，不能设置为 true
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        
        // 暴露头信息
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        // ... 更多头信息
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### WebMvcConfigurer 全局配置

每个服务都实现了 `WebMvcConfigurer`，添加了 `addCorsMappings` 方法：

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(false)
            .exposedHeaders("Authorization", "Content-Type", "X-Requested-With", 
                           "Accept", "Origin", "Access-Control-Request-Method", 
                           "Access-Control-Request-Headers")
            .maxAge(3600);
}
```

### 配置文件设置

在 `application.yml` 中配置：

```yaml
spring:
  mvc:
    cors:
      mapping: /**
      allowed-origins: "*"
      allowed-methods: 
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
        - HEAD
        - PATCH
      allowed-headers: "*"
      allow-credentials: false
      max-age: 3600
      exposed-headers: 
        - Authorization
        - Content-Type
        - X-Requested-With
        - Accept
        - Origin
        - Access-Control-Request-Method
        - Access-Control-Request-Headers
        - Access-Control-Allow-Origin
```

## 🧪 测试接口

每个服务都提供了跨域测试接口：

### 测试端点

- **User服务**: `http://localhost:9001/cors-test/`
- **Social服务**: `http://localhost:9002/cors-test/`
- **Matchmaker服务**: `http://localhost:9004/cors-test/`

### 可用的测试方法

- `GET /cors-test/get` - 测试 GET 请求
- `POST /cors-test/post` - 测试 POST 请求
- `PUT /cors-test/put` - 测试 PUT 请求
- `DELETE /cors-test/delete` - 测试 DELETE 请求
- `OPTIONS /cors-test/options` - 测试 OPTIONS 预检请求

## 🌍 前端调用示例

### JavaScript Fetch API

```javascript
// GET 请求
fetch('http://localhost:9001/cors-test/get', {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json',
    }
})
.then(response => response.json())
.then(data => console.log(data));

// POST 请求
fetch('http://localhost:9001/user/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
    },
    body: JSON.stringify({
        username: 'test',
        password: 'password'
    })
})
.then(response => response.json())
.then(data => console.log(data));
```

### Axios

```javascript
// 配置 axios 默认设置
axios.defaults.baseURL = 'http://localhost:9001';
axios.defaults.headers.common['Content-Type'] = 'application/json';

// GET 请求
axios.get('/cors-test/get')
    .then(response => console.log(response.data));

// POST 请求
axios.post('/user/login', {
    username: 'test',
    password: 'password'
})
.then(response => console.log(response.data));
```

### jQuery AJAX

```javascript
$.ajaxSetup({
    crossDomain: true,
    contentType: 'application/json'
});

// GET 请求
$.get('http://localhost:9001/cors-test/get', function(data) {
    console.log(data);
});

// POST 请求
$.ajax({
    url: 'http://localhost:9001/user/login',
    type: 'POST',
    data: JSON.stringify({
        username: 'test',
        password: 'password'
    }),
    contentType: 'application/json',
    success: function(data) {
        console.log(data);
    }
});
```

## 🔍 故障排除

### 常见问题

1. **仍然出现跨域错误**
   - 确保服务已重新启动
   - 检查浏览器控制台的具体错误信息
   - 验证请求的 URL 和端口是否正确

2. **OPTIONS 预检请求失败**
   - 检查服务器是否正确处理 OPTIONS 请求
   - 使用测试接口验证：`/cors-test/options`

3. **特定请求头被拒绝**
   - 检查 `allowedHeaders` 配置
   - 确保自定义请求头已添加到配置中

### 调试步骤

1. **测试基本连通性**
   ```bash
   curl -X GET http://localhost:9001/cors-test/get
   ```

2. **测试 OPTIONS 预检请求**
   ```bash
   curl -X OPTIONS http://localhost:9001/cors-test/options \
        -H "Origin: http://localhost:3000" \
        -H "Access-Control-Request-Method: POST" \
        -H "Access-Control-Request-Headers: Content-Type"
   ```

3. **检查响应头**
   使用浏览器开发者工具查看响应头是否包含：
   - `Access-Control-Allow-Origin: *`
   - `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH`
   - `Access-Control-Allow-Headers: *`

## 📝 注意事项

1. **生产环境配置**
   - 在生产环境中，建议将 `allowedOrigins` 设置为具体的域名，而不是 `*`
   - 根据需要调整 `allowCredentials` 设置

2. **安全考虑**
   - 当前配置允许所有域名访问，适用于开发环境
   - 生产环境应该限制允许的域名列表

3. **版本兼容性**
   - 当前配置适用于 Spring Boot 2.3.2
   - 更高版本的 Spring Boot 可能支持 `allowedOriginPatterns`

## 🚀 部署后验证

部署完成后，可以通过以下方式验证跨域配置：

1. 访问测试接口确认服务正常运行
2. 使用前端应用测试实际的 API 调用
3. 检查浏览器网络面板确认没有跨域错误

现在您的聊巴项目已经完全支持跨域请求了！🎉
