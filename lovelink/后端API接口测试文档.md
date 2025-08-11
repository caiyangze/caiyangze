# 后端API接口测试文档

## 新增接口说明

本文档描述了新增的点赞、评论、浏览量相关的API接口。

### 基础信息

- **服务地址**: `http://localhost:8082` (social模块)
- **请求头**: 需要在Header中添加 `token` 字段
- **响应格式**: JSON

### 响应格式说明

```json
{
  "code": 200,        // 状态码：200成功，其他失败
  "message": "操作成功", // 响应消息
  "data": {}          // 响应数据
}
```

## 1. 点赞相关接口

### 1.1 点赞/取消点赞动态

**接口地址**: `POST /api/social/moment/like/{momentId}`

**请求参数**:
- **路径参数**:
  - `momentId` (Long): 动态ID

**请求头**:
```
token: your_jwt_token
```

**响应示例**:
```json
{
  "code": 200,
  "message": "点赞成功",  // 或 "取消点赞成功"
  "data": null
}
```

**测试用例**:
```bash
# 点赞动态
curl -X POST "http://localhost:8082/api/social/moment/like/1" \
  -H "token: your_jwt_token"

# 再次请求会取消点赞
curl -X POST "http://localhost:8082/api/social/moment/like/1" \
  -H "token: your_jwt_token"
```

## 2. 浏览量相关接口

### 2.1 增加动态浏览次数

**接口地址**: `POST /api/social/moment/view/{momentId}`

**请求参数**:
- **路径参数**:
  - `momentId` (Long): 动态ID

**请求头**: 无需token（游客也可以浏览）

**响应示例**:
```json
{
  "code": 200,
  "message": "浏览次数增加成功",
  "data": null
}
```

**测试用例**:
```bash
# 增加浏览次数
curl -X POST "http://localhost:8082/api/social/moment/view/1"
```

## 3. 评论相关接口

### 3.1 获取动态评论列表

**接口地址**: `GET /api/social/moment/comments/{momentId}`

**请求参数**:
- **路径参数**:
  - `momentId` (Long): 动态ID
- **查询参数**:
  - `pageNum` (Integer): 页码，默认1
  - `pageSize` (Integer): 页大小，默认20

**请求头**:
```
token: your_jwt_token  // 可选，用于判断点赞状态
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "commentId": 1,
        "targetId": 1,
        "targetType": 1,
        "userId": 1,
        "nickname": "用户1",
        "avatarUrl": "",
        "content": "这是一条评论",
        "parentId": null,
        "replyUserId": null,
        "replyUserNickname": null,
        "likeCount": 0,
        "status": 1,
        "createdAt": "2025-07-23T10:00:00",
        "updatedAt": "2025-07-23T10:00:00",
        "isLiked": false,
        "replies": []
      }
    ],
    "total": 1,
    "size": 20,
    "current": 1,
    "pages": 1
  }
}
```

**测试用例**:
```bash
# 获取评论列表
curl -X GET "http://localhost:8082/api/social/moment/comments/1?pageNum=1&pageSize=10" \
  -H "token: your_jwt_token"
```

### 3.2 添加动态评论

**接口地址**: `POST /api/social/moment/comment`

**请求参数**:
- **请求体** (JSON):
```json
{
  "momentId": 1,           // 动态ID，必填
  "content": "这是评论内容", // 评论内容，必填，最大500字符
  "parentId": null,        // 父评论ID，回复评论时填写
  "replyUserId": null      // 回复用户ID，回复评论时填写
}
```

**请求头**:
```
Content-Type: application/json
token: your_jwt_token
```

**响应示例**:
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "commentId": 1,
    "targetId": 1,
    "targetType": 1,
    "userId": 1,
    "content": "这是评论内容",
    "parentId": null,
    "replyUserId": null,
    "likeCount": 0,
    "status": 1,
    "createdAt": "2025-07-23T10:00:00",
    "updatedAt": "2025-07-23T10:00:00"
  }
}
```

**测试用例**:
```bash
# 添加评论
curl -X POST "http://localhost:8082/api/social/moment/comment" \
  -H "Content-Type: application/json" \
  -H "token: your_jwt_token" \
  -d '{
    "momentId": 1,
    "content": "这是一条测试评论"
  }'

# 回复评论
curl -X POST "http://localhost:8082/api/social/moment/comment" \
  -H "Content-Type: application/json" \
  -H "token: your_jwt_token" \
  -d '{
    "momentId": 1,
    "content": "这是回复评论",
    "parentId": 1,
    "replyUserId": 1
  }'
```

## 4. 前端对接说明

### 4.1 前端API调用示例

前端已经在 `abab_uniapp/api/moment.js` 中实现了对应的API调用方法：

```javascript
// 点赞/取消点赞动态
likeMoment(momentId) {
  return http.request({
    url: `${SOCIAL_BASE_URL}/api/social/moment/like/${momentId}`,
    method: 'POST'
  });
}

// 增加动态浏览次数
incrementViewCount(momentId) {
  return http.request({
    url: `${SOCIAL_BASE_URL}/api/social/moment/view/${momentId}`,
    method: 'POST'
  });
}

// 获取动态评论列表
getMomentComments(momentId, pageNum = 1, pageSize = 10) {
  return http.request({
    url: `${SOCIAL_BASE_URL}/api/social/moment/comments/${momentId}?pageNum=${pageNum}&pageSize=${pageSize}`,
    method: 'GET'
  });
}

// 添加动态评论
addMomentComment(momentId, content) {
  return http.request({
    url: `${SOCIAL_BASE_URL}/api/social/moment/comment`,
    method: 'POST',
    data: {
      momentId,
      content
    }
  });
}
```

### 4.2 前端使用示例

```javascript
// 点赞动态
async function likeMoment(moment) {
  try {
    const momentApi = await import('@/api/moment.js');
    const result = await momentApi.default.likeMoment(moment.momentId);
    if (result.code === 200) {
      // 更新本地状态
      moment.isLiked = !moment.isLiked;
      moment.likeCount = moment.isLiked 
        ? (moment.likeCount || 0) + 1 
        : Math.max((moment.likeCount || 0) - 1, 0);
    }
  } catch (error) {
    console.error('点赞失败:', error);
  }
}

// 增加浏览次数
async function incrementViewCount(momentId) {
  try {
    const momentApi = await import('@/api/moment.js');
    await momentApi.default.incrementViewCount(momentId);
  } catch (error) {
    console.error('增加浏览次数失败:', error);
  }
}
```

## 5. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 401 | 未登录或token无效 |
| 403 | 无权限操作 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 6. 注意事项

1. **Token验证**: 除了浏览次数接口，其他接口都需要有效的JWT token
2. **幂等性**: 点赞接口具有幂等性，重复点赞会取消点赞
3. **数据一致性**: 点赞和评论操作会自动更新动态的统计数据
4. **权限控制**: 只能操作可见的动态
5. **参数验证**: 所有接口都有完整的参数验证

## 7. 数据库影响

### 7.1 涉及的表

- `tb_moment`: 动态表，更新点赞数、评论数、浏览数
- `tb_like`: 点赞表，记录点赞关系
- `tb_comment`: 评论表，存储评论内容

### 7.2 数据更新逻辑

- **点赞**: 在 `tb_like` 表中插入/删除记录，同时更新 `tb_moment` 的 `like_count`
- **评论**: 在 `tb_comment` 表中插入记录，同时更新 `tb_moment` 的 `comment_count`
- **浏览**: 直接更新 `tb_moment` 的 `view_count`

这些接口已经完全实现并可以投入使用！
