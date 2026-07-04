# VisePanda — 移动端通用技术规范

> **架构真理源之一。** 所有 Agent 开工前必须读取。
> 定义 iOS / Android 双端统一的网络、缓存、加密、错误码、存储、业务流程标准。
> Codex 制定规范，Antigravity 无条件对齐。

---

## 1. 网络层标准

### 1.1 API 请求模式

所有后端 API 调用遵循统一格式：

```
Base URL: https://go2china.space (生产)
           http://localhost:3000   (开发)

Headers:
  Content-Type: application/json

请求体: JSON
响应体: { ok: true/false, ... }
```

**错误响应统一格式：**
```typescript
{ ok: false, error: string }           // 客户端或服务端错误
{ ok: false, error: string, status: number }  // 附带 HTTP 状态码
```

**常见错误码：**
| error 值 | HTTP | 含义 | 处理方式 |
|----------|------|------|---------|
| `missing_text` | 400 | 缺少必要参数 | 提示用户输入内容 |
| `missing_image` | 400 | 缺少图片 | 提示用户选择图片 |
| `missing_audio` | 400 | 缺少音频 | 提示用户录音或选择文件 |
| `not_configured` | 503 | 服务端缺失 API Key | 显示"暂不可用"，引导用户稍后再试 |
| `upstream_error` | 502 | 上游 API 不可用 | 显示"服务繁忙"，自动降级 |
| `no_response` | 500 | AI 未返回有效结果 | 显示"AI 暂时无法回复"，提示重试 |

### 1.2 双端统一网络层要求

| 要求 | iOS (Codex) | Android (Antigravity) |
|------|-------------|----------------------|
| HTTP 库 | URLSession / Alamofire | OkHttp / Retrofit |
| 超时 | 连接 10s, 读取 30s | 连接 10s, 读取 30s |
| 重试策略 | 仅网络错误重试，最多2次，指数退避 | 同上 |
| TLS | TLS 1.2+ | TLS 1.2+ |
| Cookie | 不依赖 Cookie（Bearer token 方式） | 同上 |

### 1.3 认证方式

当前阶段使用 Supabase Auth：
- **邮箱密码登录**：`signInWithPassword` / `signUpWithPassword`
- **Google OAuth**：`signInWithGoogle`
- 无 magic link
- 无独立 `/account` 页面；唯一入口为头部图标 + 悬浮窗口

**移动端登录流程：**
1. 客户端调用 Supabase Auth
2. 成功后获取 Supabase session token
3. 后续请求通过 Supabase 的 `Authorization: Bearer <token>` 自动附加
4. 未登录时：guest 模式（localStorage / 本地文件），不崩溃

---

## 2. AI 聊天集成标准

### 2.1 Chat 入口

```
POST /api/chat
```

**请求体字段映射（双端一致）：**
```typescript
{
  message: string,                    // 用户输入文本
  trip?: TripState,                   // 当前画布，JSON 序列化
  messages?: ChatMessage[],           // 最近对话历史
  preferenceProfile?: UserPreferenceProfile  // 偏好画像
}
```

### 2.2 画布变更流程（核心管道）

所有画布修改必须走以下路径，**不得绕过**：

```
用户在 Chat 输入消息
  → POST /api/chat
    → 收到 CanvasPatch
      → applyCanvasPatch() 将 patch 合并到当前 TripState
        → UI 刷新显示新画布
```

**端侧严格禁止：**
- 禁止在 Explore / Tools / Community 等非 Chat 组件中直接修改 TripState
- 禁止跳过 AI pipeline 拼装 TripDay
- 禁止从本地 mock 数据直接写入画布

### 2.3 输入输出约定

**输入：** 文本消息（当前阶段无图片/语音输入聊天，仅在 `/translate` 中有独立 OCR/STT）

**输出：**
```typescript
// 收到的 Chat 响应
{
  ok: true,
  patch: CanvasPatch,          // 画布变更（核心数据）
  suggestions: string[],       // 2 个建议追问
  mode: "mock" | "deepseek" | "glm",  // 使用的 AI provider
  modelLabel: string,          // 具体模型名
}
```

---

## 3. 数据存储标准

### 3.1 本地存储

| 数据 | iOS | Android | 说明 |
|------|-----|---------|------|
| 登录 session | Supabase SDK 自动管理 | Supabase SDK 自动管理 | 无需手动保存 token |
| 用户偏好设置 | UserDefaults | SharedPreferences | step 等基本设置 |
| 选中头像 ID | UserDefaults (key: `visepanda:selected-avatar`) | SharedPreferences (key: `visepanda:selected-avatar`) | |
| 草稿 Trip | 本地 SQLite / JSON 文件 | Room / JSON 文件 | 未登录时用本地存储 |
| 聊天历史 | 本地 SQLite | Room | 未登录时本地存储 |

### 3.2 远程持久化

- 所有 Supabase 读写通过 REST API 调用服务端代理路由
- **端侧不要直接拼接 Supabase 查询**，通过 Web 端 `/api/trips` 路由或未来移动端专属代理
- guest draft 登录后自动迁移（参考 `lib/supabase/tripsRepository.ts` 迁移逻辑）

### 3.3 降级策略

| 场景 | 行为 |
|------|------|
| 未登录 | 使用本地存储，功能完全可用 |
| 网络不可用 | 使用本地缓存/上次同步数据 |
| 服务端 5xx | 降级到本地 mock 或缓存数据，不崩溃 |
| API Key 未配置 | 优雅降级，显示"暂不可用"，引导稍后 |

---

## 4. 通用 UI/UX 约束

### 4.1 导航结构

| Tab | 图标 (lucide-react) | 路由 |
|-----|---------------------|------|
| Chat | MessageSquare | `/chat` |
| Trips | Map | `/trips` |
| Explore | Compass | `/explore` |
| Tools | Wrench | `/tools` |
| Translate | Languages | `/translate` |
| Community | Globe | `/community` |

### 4.2 移动端布局规则

- 导航为 `position: fixed; bottom: 0`（底部固定）
- 高度 56–64px，需要适配 `safe-area-inset-bottom`
- 激活态指示器：Tab 顶部 2px border
- 页面保持一屏高，内部滚动
- Chat / Trips / Explore / Tools / Translate / Community 各自内部可滚动

### 4.3 视觉约束

- 不使用半透明玻璃效果
- 输入框使用 solid paper-style
- 分割线使用 fine ink dividers
- 水墨背景保留（CSS 场景层叠加 `ink-landscape.png`）
- 彩色熊猫头像不改变

---

## 5. TTS / STT 规范

### 5.1 TTS（文字转语音）

- **首选：** 服务端 `/api/translate/tts`（阿里云 DashScope Qwen TTS）
- **移动端 fallback：** 原生平台 TTS API
  - iOS: `AVSpeechSynthesizer`
  - Android: `TextToSpeech`
- 传入 `text` + `voice`（可选，默认 Cherry）
- 返回 `audioUrl`（服务端生成的音频 URL）

### 5.2 STT（语音转文字）

- **路径：** `/api/translate/stt`
- 支持 audio URL 和 audio Base64 两种方式
- **iOS:** 录音为 M4A → Base64 → 发送
- **Android:** 录音为 MP3/AMR → Base64 → 发送
- 服务端返回识别文本

---

## 6. 错误处理规范

### 6.1 统一格式

```typescript
// 移动端 Error 对象统一字段
{
  code: string;          // 机器可读错误码
  message: string;       // 用户可读错误信息（英文/中文均需准备）
  recoverable: boolean;  // 是否可重试
  detail?: string;       // 调试用详情（不上报用户）
}
```

### 6.2 错误码体系

| 错误码 | 含义 | 用户提示 |
|--------|------|---------|
| `NETWORK_ERROR` | 网络不可达 | "请检查网络连接" |
| `TIMEOUT` | 请求超时 | "请求超时，请重试" |
| `SERVER_ERROR` | 服务端异常 | "服务暂时不可用" |
| `AUTH_EXPIRED` | 登录过期 | "请重新登录" |
| `PARAM_INVALID` | 参数错误 | "输入有误，请检查" |
| `AI_UNAVAILABLE` | AI 服务不可用 | "AI 助手暂时无法回复" |
| `MOCK_FALLBACK` | 降级到 mock 模式 | 无需提示用户 |
| `UPSTREAM_DOWN` | 第三方 API 不可用 | "此功能暂不可用" |

### 6.3 错误处理原则

1. 所有网络请求必须有 `try/catch`
2. 任何失败必须有用户可读的提示（toast / snackbar / alert）
3. mock 降级静默进行，不打扰用户
4. 5xx 错误自动重试最多 1 次（非幂等请求除外）
5. 所有错误必须日志记录（console / logcat）

---

## 7. Explore / Tools / Translate 落地规则

### 7.1 Explore 模块

- 数据源：`/api/explore/amap?city=X&category=Y`
- 端侧不要缓存 POI 数据超过 1 小时（可能过期）
- Add to Trip 必须走 `「点击 → 生成草稿消息 → POST /api/chat → CanvasPatch」` 流程
- 端侧不得直接在 Explore 页面中拼装 TripDay

### 7.2 Tools 模块

- 数据源：`lib/tools/staticProvider.ts`（静态）+ `/api/exchange-rate`（实时汇率）
- 工具分类固定 7 个：visa / payment / translate / currency / metro / esim-vpn / emergency
- 分类深链格式：`/tools?category=<tool-category-id>`

### 7.3 Translate 模块

- 三功能合一：文字翻译 / 扫描翻译 / 短语词典
- 所有翻译 API 走服务端代理，端侧不传 API Key
- 短语词典使用静态数据，无需联网

---

## 8. 打包与发布

### 8.1 iOS (Codex)

- 工具：Xcode + SwiftUI / UIKit
- 最低版本：iOS 16.0
- 构建产物：IPA → TestFlight → App Store

### 8.2 Android (Antigravity)

- 工具：Android Studio + Kotlin + Jetpack Compose + Material 3
- 最低版本：API 28 (Android 9.0)
- 构建产物：APK / AAB → Google Play / 侧载

### 8.3 版本号对齐

- 与 Web 端统一版本号 `0.3.x`
- 每次发布前更新 `versionName`，与仓库 `package.json` 中的版本号保持一致
- `versionCode` / `CFBundleVersion` 自增整数，不做跨端对齐

---

## 9. 变更流程

1. Codex 发现需要新增双端规范 → 更新此文档，提交 PR 给 Claude Code 审核
2. Antigravity 发现规范不适用或缺失 → 上报【架构冲突上报】，Codex 裁定后更新规范
3. 双端实现必须严格对齐此文档，差异视为合规缺陷

---

*初始版本生成于 2026-07-04，基于 AGENTS.md v0.3.x + 代码库现有模式提取。*
*Codex 维护技术规范内容，Claude Code 终审。*
