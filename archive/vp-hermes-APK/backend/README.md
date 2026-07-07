# VisePanda Backend
# 开发者快速启动指南

## 1. 启动依赖（Docker）
```bash
docker-compose up -d
```

## 2. 启动 API 服务（开发模式）
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 3. 访问 API 文档
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 4. API 概览

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | No | 邮箱注册 |
| POST | /api/auth/login | No | 邮箱密码登录 |
| POST | /api/auth/logout | Yes | 登出 |
| POST | /api/auth/verify-email | No | 邮箱验证 |
| POST | /api/auth/forgot-password | No | 忘记密码 |
| POST | /api/auth/reset-password | No | 重置密码 |
| GET | /api/auth/me | Yes | 当前用户信息 |

### Destinations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/destinations | 城市列表（搜索+筛选） |
| GET | /api/destinations/{id} | 城市详情 |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/chat/message | SSE 流式对话（当前为 mock） |

### Trips (需认证)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/trips | 我的行程列表 |
| POST | /api/trips | 创建/保存行程 |
| DELETE | /api/trips/{id} | 删除行程 |

### Admin (需 admin 角色)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/admin/stats | 用户统计 |
| GET | /api/admin/users | 用户列表 |
| GET | /api/admin/users/{id} | 用户详情 |
| PATCH | /api/admin/users/{id} | 编辑用户 |

## 5. 环境变量
在 `.env` 文件中设置（可选，有默认值）：
```
DATABASE_URL=postgresql://visepanda:visepanda@localhost:5432/visepanda
JWT_SECRET=your-secret-here
DEBUG=true
```

## 6. 创建管理员账号
启动后，通过注册 API 创建账号，然后在数据库中手动设置 role=admin：
```sql
UPDATE users SET role='admin', status='active' WHERE email='your@email.com';
```
