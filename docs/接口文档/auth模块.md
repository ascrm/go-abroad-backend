# Auth 模块接口文档

## 基础信息

| 项目 | 说明 |
|------|------|
| 基础路径 | `/auth` |
| 认证方式 | Token (JWT) |

---

## 1. 账号密码登录

### 请求

```http
POST /auth/login
Content-Type: application/json
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | 是 | 用户名 / 邮箱 / 手机号 |
| password | String | 是 | 密码 |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://example.com/avatar.jpg",
      "gender": 1
    }
  }
}
```

### 响应参数

| 参数 | 类型 | 说明 |
|------|------|------|
| code | Integer | 业务状态码：20000-成功，50000-失败 |
| message | String | 提示信息 |
| accessToken | String | 访问令牌 |
| refreshToken | String | 刷新令牌 |
| expiresIn | Long | 过期时间（毫秒） |
| user.userId | Long | 用户ID |
| user.username | String | 用户名 |
| user.nickname | String | 昵称 |
| user.avatar | String | 头像URL |
| user.gender | Integer | 性别：0-未知, 1-男, 2-女 |

---

## 2. 发送验证码

### 请求

```http
POST /auth/sendCode
Content-Type: application/json
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountType | Integer | 是 | 账号类型：2-邮箱，3-手机号 |
| account | String | 是 | 邮箱或手机号 |
| codeType | Integer | 是 | 验证码类型：1-注册，2-登录，3-找回密码 |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "codeId": "",
    "code": "123456",
    "expiresIn": 300,
    "target": "te***@example.com"
  }
}
```

### 响应参数

| 参数 | 类型 | 说明 |
|------|------|------|
| codeId | String | 验证码ID（暂未使用） |
| code | String | 验证码（开发环境返回） |
| expiresIn | Long | 验证码过期时间（秒） |
| target | String | 账号掩码 |

### 使用示例

#### 发送邮箱验证码

```bash
curl -X POST http://localhost:8080/auth/sendCode \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": 2,
    "account": "test@example.com",
    "codeType": 1
  }'
```

#### 发送手机验证码

```bash
curl -X POST http://localhost:8080/auth/sendCode \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": 3,
    "account": "13800138000",
    "codeType": 1
  }'
```

---

## 3. 邮箱/手机号注册

> ⚠️ 注意：注册前需要先调用 `/auth/sendCode` 获取验证码

### 请求

```http
POST /auth/register
Content-Type: application/json
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | 是 | 邮箱或手机号 |
| password | String | 是 | 密码（6-20位） |
| code | String | 是 | 验证码 |
| accountType | Integer | 否 | 账号类型：2-邮箱，3-手机号（不传则自动判断） |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "userId": 1,
      "username": "user12345678",
      "nickname": "test",
      "avatar": null,
      "gender": 0
    }
  }
}
```

### 使用示例

#### 邮箱注册

```bash
# 1. 先获取验证码
curl -X POST http://localhost:8080/auth/sendCode \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": 2,
    "account": "test@example.com",
    "codeType": 1
  }'

# 2. 使用验证码注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "account": "test@example.com",
    "password": "123456",
    "code": "123456"
  }'
```

#### 手机号注册

```bash
# 1. 先获取验证码
curl -X POST http://localhost:8080/auth/sendCode \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": 3,
    "account": "13800138000",
    "codeType": 1
  }'

# 2. 使用验证码注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "account": "13800138000",
    "password": "123456",
    "code": "123456"
  }'
```

---

## 4. 第三方注册（Google/Apple）

### 请求

```http
POST /auth/social/register
Content-Type: application/json
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| socialType | Integer | 是 | 平台类型：3-Google, 4-Apple |
| openid | String | 是 | 第三方平台 openid |
| unionid | String | 否 | Apple unionid |
| accessToken | String | 否 | 第三方平台 access_token |
| refreshToken | String | 否 | 第三方平台 refresh_token |
| expiresIn | Long | 否 | 令牌过期时间（秒） |
| nickname | String | 否 | 用户昵称 |
| avatar | String | 否 | 用户头像 |
| gender | Integer | 否 | 性别：0-未知, 1-男, 2-女 |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "userId": 1,
      "username": "google12345678",
      "nickname": "张三",
      "avatar": "https://example.com/avatar.jpg",
      "gender": 1
    }
  }
}
```

### 使用示例

#### Google 注册

```bash
curl -X POST http://localhost:8080/auth/social/register \
  -H "Content-Type: application/json" \
  -d '{
    "socialType": 3,
    "openid": "google_openid_xxx",
    "accessToken": "google_access_token",
    "nickname": "张三"
  }'
```

#### Apple 注册

```bash
curl -X POST http://localhost:8080/auth/social/register \
  -H "Content-Type: application/json" \
  -d '{
    "socialType": 4,
    "openid": "apple_openid_xxx",
    "unionid": "apple_unionid_xxx",
    "nickname": "李四"
  }'
```

---

## 5. 第三方登录

前端只需传递平台类型和授权码，后端自动换取 token 并获取用户信息

### 请求

```http
POST /auth/social/login
Content-Type: application/json
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| socialType | Integer | 是 | 平台类型：1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音 |
| code | String | 是 | 授权码（前端从第三方平台获取的一次性 code） |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "userId": 1,
      "username": "social_abc12345",
      "nickname": "微信用户",
      "avatar": "https://example.com/avatar.jpg",
      "gender": 1
    }
  }
}
```

---

## 6. 刷新 Token

### 请求

```http
POST /auth/refresh
Refresh-Token: <refresh_token>
```

### 请求头

| 参数 | 必填 | 说明 |
|------|------|------|
| Refresh-Token | 是 | 刷新令牌 |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://example.com/avatar.jpg",
      "gender": 1
    }
  }
}
```

---

## 7. 退出登录

### 请求

```http
POST /auth/logout
Authorization: Bearer <access_token>
```

### 请求头

| 参数 | 必填 | 说明 |
|------|------|------|
| Authorization | 是 | 访问令牌 |

### 响应

```json
{
  "code": 20000,
  "message": "success",
  "data": null
}
```

### 使用示例

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

> **注意**：由于 JWT 是无状态的，退出登录后前端需要自行删除存储的 accessToken 和 refreshToken。

---

## 响应码说明

### 业务状态码（code）

| 状态码 | 说明 |
|--------|------|
| 20000 | 操作成功 |
| 50000 | 操作失败（业务异常） |

### HTTP 状态码

所有接口的 HTTP 状态码统一为 200，通过业务状态码（code）区分成功和失败。

### 错误码详细说明

| 业务码 | 说明 |
|--------|------|
| 50000 | 账号或密码错误 |
| 50000 | 账号已被禁用 |
| 50000 | 无效的刷新令牌 |
| 50000 | 用户不存在 |
| 50000 | 该账号已被注册 |
| 50000 | 该第三方账号已被注册 |
| 50000 | 不支持的第三方平台 |
| 50000 | 邮箱格式不正确 |
| 50000 | 手机号格式不正确 |
| 50000 | 不支持的账号类型 |
| 50000 | 验证码错误或已过期 |
| 50000 | 邮件发送失败，请稍后重试 |
| 50000 | 短信发送失败，请稍后重试 |

---

## 使用示例

### cURL

#### 发送验证码

```bash
curl -X POST http://localhost:8080/auth/sendCode \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": 2,
    "account": "test@example.com",
    "codeType": 1
  }'
```

#### 账号密码登录

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "admin@example.com",
    "password": "123456"
  }'
```

#### 第三方登录

```bash
curl -X POST http://localhost:8080/auth/social/login \
  -H "Content-Type: application/json" \
  -d '{
    "socialType": 3,
    "code": "授权码"
  }'
```

#### 刷新 Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 访问受保护接口

```bash
curl http://localhost:8080/user/info \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```
