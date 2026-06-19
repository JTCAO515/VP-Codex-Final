# Vercel 部署说明（vpadmin.go2china.space）

本仓库是一个 pnpm workspace 的 monorepo，包含两个 Next.js 后台与一个 NestJS API。

GitHub 仓库：

- `https://github.com/JTCAO515/VP-Trae-Admin`

## 1. 部署对象

- `apps/admin-web`：管理后台（建议绑定域名 `vpadmin.go2china.space`）
- `apps/ops-web`：运营后台（建议单独创建一个 Vercel Project；域名按需要另配）

> 说明：`apps/api` 是 NestJS 项目，不建议直接部署到 Vercel（除非单独做 serverless 适配）。通常做法是将 API 部署到独立运行环境（VM / 容器 / PaaS），然后在 Vercel 里配置 `API_BASE_URL` 指向该 API 公网地址。

## 2. 创建 Vercel Project

推荐做法：**一个应用一个 Project**（更清晰，也方便绑定域名与环境变量）。

### 2.1 admin-web（vpadmin.go2china.space）

1. 在 Vercel 选择该 GitHub 仓库新建 Project
2. Root Directory 选择：`apps/admin-web`
3. Framework 选择：Next.js（通常会自动识别）
4. 环境变量（Production/Preview/Development 都建议配置）：
   - `API_BASE_URL`：统一 API 的公网地址
5. 绑定域名：在 Project 的 Domains 中绑定 `vpadmin.go2china.space`

### 2.2 ops-web

步骤同上，Root Directory 选择：`apps/ops-web`，环境变量同样需要 `API_BASE_URL`。

## 3. 环境变量清单

后台 Web（admin-web / ops-web）：

- `API_BASE_URL`（必填）：统一 API 的公网地址

统一 API（apps/api）：

- 不在 Vercel 部署时，按你选择的部署平台配置其运行环境变量（可参考根目录 `.env.example`）

## 4. 发布检查

1. Vercel 构建日志中应能看到 `pnpm install` 与 `next build`
2. 访问 `/login` 正常渲染
3. 登录后跳转到后台首页，且页面内请求能成功代理到 `API_BASE_URL`
