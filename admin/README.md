# VisePanda Admin Web 管理后台

基于 React + Vite + TypeScript 构建的管理后台，对接 VisePanda 后端 API。

## 启动

```bash
cd admin
npm install
npm run dev     # 开发模式，默认 :3000
npm run build   # 生产构建
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_URL` | API 基础地址 | `/`（开发环境通过 Vite proxy 代理到 localhost:8000） |

## 功能

- 管理员登录（JWT）
- Dashboard 用户统计
- 用户列表（搜索+筛选+分页）
- 用户详情/编辑

## 技术栈

- React 19 + TypeScript
- Vite 8
- React Router 7
- TanStack React Query 5
- Axios
- Tailwind CSS 4
