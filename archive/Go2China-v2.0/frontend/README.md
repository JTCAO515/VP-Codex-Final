# VisePanda AI MVP（前端静态Demo）

这是一个零依赖的手机网页 Demo（`index.html`），用于快速验证：
- 聊天调用 `/chat/messages`
- 渲染 ask action 的选项按钮
- 点击后继续对话
- 展示 itinerary_updated 的行程 JSON

## 使用方式

1) 启动后端（见 `backend/README.md`），默认端口 8000。
2) 用任意静态文件服务器打开本页面，或者直接在浏览器打开文件也可（部分浏览器会限制跨域）。

如果需要本地静态服务器，可在 `frontend/` 下运行：

```bash
python3 -m http.server 5173
```

然后打开：
- http://localhost:5173/

页面右侧可配置 backend 地址、user_id、trip_id。

## 线上（Vercel）说明
- 若前后端同域名部署到 Vercel：右侧 Backend 留空即可（会自动使用 `window.location.origin` 并调用 `/api/...`）。
