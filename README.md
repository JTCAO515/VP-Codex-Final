# vp-web-trae

这个仓库只承载 **用户端（游客端）** Web 前台，对应线上站点：`vp.go2china.space`。

代码位置：

- `apps/traveler-web`：游客前台（Next.js）

## Vercel 部署

建议在 Vercel 创建一个项目并指向本仓库，Root Directory 选择 `apps/traveler-web`。

需要配置环境变量：

- `API_BASE_URL`：统一 API 的公网地址（例如后台仓库部署出来的 API 地址）

## 本地启动

```bash
pnpm install
pnpm --filter traveler-web dev
```

默认访问：

- `http://localhost:3100`
