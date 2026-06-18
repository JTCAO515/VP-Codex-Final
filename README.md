# VisePanda Admin Monorepo

这个仓库只承载管理系统相关工程，用于部署后台域名 `vpadmin.go2china.space`。

包含：

- `apps/ops-web`：内容运营后台
- `apps/admin-web`：管理后台
- `apps/api`：后台与前台共用的统一 API
- `packages/*`：后台依赖的领域包与共享类型

不包含：

- `apps/traveler-web`
- `apps/traveler-android`

## 本地启动

```bash
pnpm install
pnpm --filter api start:dev
pnpm --filter ops-web dev
pnpm --filter admin-web dev
```

默认端口：

- `api`: `http://localhost:3000`
- `ops-web`: `http://localhost:3101`
- `admin-web`: `http://localhost:3102`
