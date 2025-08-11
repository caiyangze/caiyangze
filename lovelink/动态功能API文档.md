# 动态功能API文档

## 概述

本文档描述了用户动态发布功能的所有API接口，包括动态的增删改查、图片上传、隐私设置等功能。

## 基础信息

- **服务名称**: lovelink-social
- **服务端口**: 9002
- **基础路径**: `/api/social`
- **认证方式**: JWT Token（通过请求头 `token` 传递）

## API接口列表

### 1. 动态管理接口

#### 1.1 发布动态
- **接口**: `POST /api/social/moment/create`
- **描述**: 发布新动态，支持文字、图片等多媒体内容
- **请求方式**: `multipart/form-data`
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  Content-Type: multipart/form-data
  ```
- **请求参数**:
  ```
  content: string (必填) - 动态内容，最大1000字符
  location: string (可选) - 位置信息，最大100字符
  visibility: integer (可选) - 可见范围：1-公开，2-仅关注者可见，3-仅自己可见，默认1
  mediaFiles: file[] (可选) - 媒体文件列表，最多9个文件
  topicIds: long[] (可选) - 话题ID列表
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "momentId": 1,
      "userId": 123,
      "content": "今天天气真好！",
      "location": "北京市朝阳区",
      "visibility": 1,
      "likeCount": 0,
      "commentCount": 0,
      "viewCount": 0,
      "status": 1,
      "createdAt": "2025-07-21T10:30:00",
      "updatedAt": "2025-07-21T10:30:00"
    }
  }
  ```

#### 1.2 更新动态
- **接口**: `PUT /api/social/moment/update`
- **描述**: 更新已发布的动态内容
- **请求方式**: `application/json`
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  Content-Type: application/json
  ```
- **请求参数**:
  ```json
  {
    "momentId": 1,
    "content": "更新后的动态内容",
    "location": "新的位置信息",
    "visibility": 2
  }
  ```

#### 1.3 删除动态
- **接口**: `DELETE /api/social/moment/delete/{momentId}`
- **描述**: 删除指定的动态（软删除）
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  ```

#### 1.4 查询动态详情
- **接口**: `GET /api/social/moment/detail/{momentId}`
- **描述**: 查询指定动态的详细信息
- **请求头**: 
  ```
  token: {JWT_TOKEN} (可选，游客也可查看公开动态)
  ```

#### 1.5 查询公开动态列表
- **接口**: `GET /api/social/moment/public`
- **描述**: 分页查询所有公开动态
- **请求参数**:
  ```
  pageNum: integer (可选) - 页码，默认1
  pageSize: integer (可选) - 页大小，默认10
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "momentId": 1,
          "userId": 123,
          "nickname": "用户昵称",
          "avatarUrl": "头像URL",
          "content": "动态内容",
          "location": "位置信息",
          "visibility": 1,
          "likeCount": 5,
          "commentCount": 3,
          "viewCount": 100,
          "status": 1,
          "createdAt": "2025-07-21T10:30:00",
          "updatedAt": "2025-07-21T10:30:00",
          "mediaList": [
            {
              "mediaId": 1,
              "mediaType": 1,
              "mediaUrl": "图片URL",
              "sortOrder": 1
            }
          ],
          "isLiked": false,
          "isMine": false
        }
      ],
      "total": 100,
      "size": 10,
      "current": 1,
      "pages": 10
    }
  }
  ```

#### 1.6 查询用户自己的动态
- **接口**: `GET /api/social/moment/mine`
- **描述**: 分页查询当前用户的所有动态
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  ```
- **请求参数**:
  ```
  pageNum: integer (可选) - 页码，默认1
  pageSize: integer (可选) - 页大小，默认10
  ```

#### 1.7 更新动态可见性
- **接口**: `PUT /api/social/moment/visibility/{momentId}`
- **描述**: 更新动态的可见性设置
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  ```
- **请求参数**:
  ```
  visibility: integer - 可见范围：1-公开，2-仅关注者可见，3-仅自己可见
  ```

### 2. 文件上传接口

#### 2.1 上传单张图片
- **接口**: `POST /api/social/file/upload/moment`
- **描述**: 上传动态图片
- **请求方式**: `multipart/form-data`
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  Content-Type: multipart/form-data
  ```
- **请求参数**:
  ```
  file: file - 图片文件，支持jpg、png、gif、webp格式，最大10MB
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": "http://localhost:9000/lovelink/moment/20250721/image_123456.jpg"
  }
  ```

#### 2.2 批量上传图片
- **接口**: `POST /api/social/file/upload/moment/batch`
- **描述**: 批量上传动态图片，最多9张
- **请求方式**: `multipart/form-data`
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  Content-Type: multipart/form-data
  ```
- **请求参数**:
  ```
  files: file[] - 图片文件数组，最多9个文件
  ```

#### 2.3 上传视频
- **接口**: `POST /api/social/file/upload/video`
- **描述**: 上传动态视频
- **请求方式**: `multipart/form-data`
- **请求头**: 
  ```
  token: {JWT_TOKEN}
  Content-Type: multipart/form-data
  ```
- **请求参数**:
  ```
  file: file - 视频文件，支持mp4、avi、mov等格式，最大100MB
  ```

### 3. 测试接口

#### 3.1 服务健康检查
- **接口**: `GET /api/social/test/hello`
- **描述**: 检查服务是否正常运行
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": "Social服务运行正常！"
  }
  ```

## 错误码说明

- **200**: 成功
- **400**: 请求参数错误
- **401**: 未登录或token无效
- **403**: 权限不足
- **404**: 资源不存在
- **500**: 服务器内部错误

## 使用示例

### 发布带图片的动态

```javascript
// 1. 先上传图片
const formData = new FormData();
formData.append('file', imageFile);

const uploadResponse = await fetch('/api/social/file/upload/moment', {
  method: 'POST',
  headers: {
    'token': 'your_jwt_token'
  },
  body: formData
});

const imageUrl = await uploadResponse.json();

// 2. 发布动态
const momentData = new FormData();
momentData.append('content', '今天天气真好！');
momentData.append('location', '北京市朝阳区');
momentData.append('visibility', '1');
momentData.append('mediaFiles', imageFile);

const momentResponse = await fetch('/api/social/moment/create', {
  method: 'POST',
  headers: {
    'token': 'your_jwt_token'
  },
  body: momentData
});
```

### 查询公开动态

```javascript
const response = await fetch('/api/social/moment/public?pageNum=1&pageSize=10', {
  method: 'GET',
  headers: {
    'token': 'your_jwt_token' // 可选
  }
});

const moments = await response.json();
```

## 注意事项

1. 所有需要用户身份验证的接口都需要在请求头中携带有效的JWT token
2. 文件上传接口有大小和格式限制，请注意检查
3. 动态的可见性设置会影响其他用户是否能查看该动态
4. 删除动态是软删除，数据不会真正从数据库中删除
5. 图片上传后会返回MinIO的访问URL，可直接用于前端显示
