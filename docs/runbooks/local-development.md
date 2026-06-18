# 本地开发说明

本文档定义 VisePanda 管理系统仓库的本地启动方式、依赖准备和推荐开发顺序。

## 1. 前置要求

- Node.js 20+
- pnpm 10+
- Docker 与 Docker Compose
- 可选：`psql` 用于手动执行 SQL 迁移

## 2. 初始化工程

在仓库根目录执行：

```bash
pnpm install
```

该命令会安装根工作区开发依赖，并校验 `pnpm-workspace.yaml` 是否可正常解析。

## 3. 环境变量

1. 复制根目录样例文件：

```bash
cp .env.example .env
```

2. 按本机环境调整以下变量：
   - `DATABASE_URL`
   - `REDIS_URL`
   - `S3_ENDPOINT`
   - `S3_ACCESS_KEY`
   - `S3_SECRET_KEY`
   - `API_BASE_URL`（Web 端代理到统一 API 时使用，默认 `http://localhost:3000`）

## 4. 启动本地基础依赖

```bash
pnpm docker:up
```

停止并清理依赖：

```bash
pnpm docker:down
```

在修改 Compose 配置后，可先执行：

```bash
pnpm docker:check
```

## 5. 推荐启动顺序

当前建议按以下顺序启动：

1. 本地依赖：PostgreSQL、Redis、MinIO
2. 数据迁移：执行 `infra/migrations` 中的初始化脚本
3. API：`apps/api`
4. Web 后台：`apps/ops-web`
5. 管理后台：`apps/admin-web`

原因：

- API 负责统一提供鉴权、内容、AI 与行程能力
- `ops-web` 与 `admin-web` 共同依赖统一 API
- 内容、认证、AI、Trip 等领域逻辑统一收敛在 API 与 `packages/`

## 6. 数据迁移入口

在正式迁移工具接入前，先使用 SQL 文件作为入口。示例：

```bash
psql "$DATABASE_URL" -f infra/migrations/001_auth_init.sql
```

后续若接入 Prisma 或 NestJS CLI 脚本，应继续保留本文件作为统一入口说明。

## 7. 常见开发动作

```bash
pnpm lint
pnpm test
pnpm build
```

## 8. 常用启动命令

```bash
pnpm --filter api start:dev
pnpm --filter ops-web dev
pnpm --filter admin-web dev
```

默认端口：

- `api`: `3000`
- `ops-web`: `3101`
- `admin-web`: `3102`

说明：

- 当前仓库仅包含后台相关工程
- 若对接独立游客端仓库，请将其 `API_BASE_URL` 指向这里启动的 `apps/api`
