# VisePanda iOS 原生 App 开发说明

这份 iOS 工程是原生 SwiftUI App，不是 WebView 套壳。当前版本是 MVP：五个主 Tab、AI Butler Chat、Trips、Explore、Tools、Me、本地 starter/static fallback、HTTP 调用后端 `/api/chat`。

## 1. 怎么打开工程

1. 打开 Finder。
2. 进入项目目录：`/Users/jtcao/Documents/Codex 2/VP-Codex-Final/ios`
3. 双击 `VisePandaIOS.xcodeproj`。
4. Xcode 打开后，左上角 Scheme 选择 `VisePandaIOS`。
5. 设备选择一个 iPhone 模拟器，例如 `iPhone 16 Pro`。
6. 点击左上角三角形 Run 按钮。

如果 Xcode 提示 Command Line Tools 没设置：

1. 打开 Xcode。
2. 顶部菜单选择 `Xcode` → `Settings...`。
3. 进入 `Locations`。
4. `Command Line Tools` 选择当前 Xcode 版本。
5. 关掉设置窗口，再重新运行。

## 2. 当前已经实现了什么

- `Chat`：默认启动页，居中突出底部按钮，调用后端 `/api/chat`。失败时不改行程，显示明确的 live Butler 不可用提示。
- `Trips`：展示当前 `TripState`。没有后端生成行程时显示 starter canvas，不伪造每日计划。
- `Explore`：展示本地静态 starter POI，`Add to Plan` 会跳回 Chat，让后端 Butler 判断是否加入行程。
- `Tools`：旅行工具宫格，包括翻译、支付、交通、汇率、eSIM、入境清单、紧急、离线包。翻译/汇率可调用现有 `/api/*`，失败时显示不可用状态。
- `Me`：本地游客资料、偏好、当前 trip、本地离线缓存状态。
- 本地存储：当前用 `UserDefaults` 保存 trip/messages/suggestions，后续可升级为 SwiftData 或 SQLite。
- 设计系统：已对齐纸色 `#FAF8F4`、朱砂红 `#C1292E`、金色 `#C9A84C`、黑墨色 `#1C1410`。
- 字体：已复制 Android 端的 `DM Sans` 和 `Playfair Display` 到 iOS 工程。

## 3. 后端 API 配置

当前 iOS 默认请求：

`https://go2china.space/api/chat`

同时复用：

- `POST /api/translate/text`
- `GET /api/exchange-rate`

后续要切换测试环境时，打开：

`ios/VisePandaIOS/Data/VisePandaAPIClient.swift`

修改 `baseURL` 为你的测试域名，例如：

`https://your-preview-domain.vercel.app`

注意：DeepSeek、OpenAI、Aliyun Bailian、Supabase service role、汇率服务等 API key 都不要放在 iOS App 里。iOS 只调用你的后端 `/api/*`，密钥继续留在 Vercel 环境变量里。

`/api/chat` 请求字段当前对齐 `API_SPEC.md`：

- `message`
- `trip`
- `messages`
- `preferenceProfile`

`/api/chat` 成功响应里 iOS 会解码这些字段：

- `ok`
- `mode`
- `modelLabel`
- `intent`
- `strategy`
- `providersTried`
- `patch`
- `suggestions`
- `toolContext`

iOS 只用 `patch` 通过 `CanvasPatchApplier` 合并本地 `TripState`。端侧不会自己拼装 AI 生成的 `TripDay`。

## 4. 如果运行时签名报错

模拟器通常不需要付费开发者账号；真机运行才需要签名。

真机步骤：

1. Xcode 左侧点蓝色项目图标 `VisePandaIOS`。
2. 中间选择 Target：`VisePandaIOS`。
3. 进入 `Signing & Capabilities`。
4. 勾选 `Automatically manage signing`。
5. `Team` 选择你的 Apple ID。
6. 如果没有 Team，点 `Add Account...` 登录 Apple ID。
7. `Bundle Identifier` 如果重复，把 `space.go2china.visepanda.ios` 改成你自己的，例如 `com.jtcao.visepanda.ios`。
8. 再点 Run。

## 5. 权限说明

`Info.plist` 已经提前写入这些权限文案：

- Camera：未来 OCR 翻译、证件/菜单扫描。
- Microphone：未来语音输入、语音翻译。
- Speech Recognition：未来 STT。
- Photo Library：未来从相册选图片做 OCR。
- Location：未来附近推荐、地图和交通建议。

当前 MVP 不会主动弹这些权限。等后续接相机、语音、地图时，iOS 会在第一次使用对应功能时弹窗。

## 6. 后续需要你手动申请或配置的东西

### Apple Developer

如果只用模拟器，不急着买 Apple Developer Program。
如果要上 TestFlight 或 App Store，需要加入 Apple Developer Program。

步骤：

1. 打开 <https://developer.apple.com/programs/>
2. 使用你的 Apple ID 登录。
3. 点击 Enroll。
4. 选择 Individual 或 Organization。
5. 完成付款和身份验证。
6. 回到 Xcode，在 `Signing & Capabilities` 选择你的 Team。

### 地图 SDK

中国境内建议优先考虑高德地图 iOS SDK。后续接入步骤会是：

1. 注册高德开放平台账号。
2. 创建 iOS 应用。
3. 填写 Bundle ID。
4. 申请 iOS Key。
5. 用 Swift Package Manager 或手动 framework 接入 SDK。
6. 在 iOS 代码里只放地图 SDK key；AI/后端密钥仍放服务器。

### OAuth 登录

如果以后做 Google / Apple 登录：

1. Apple 登录需要在 Apple Developer 后台开启 `Sign in with Apple` capability。
2. Google 登录需要去 Google Cloud Console 创建 iOS OAuth Client。
3. iOS 的 Bundle ID 必须和后台配置完全一致。
4. 登录成功后，建议仍由后端/Supabase 管理 session，不要把服务端密钥放 iOS。

### Supabase

后续如果 iOS 直接读写 Supabase：

1. 可以在 iOS 使用 Supabase Swift SDK。
2. iOS 只能放 `anon public key`。
3. 不要放 `service_role key`。
4. 数据权限靠 Supabase RLS policy 控制。

## 7. 离线策略

当前 MVP 的离线策略是：

- 首次启动使用 `StarterTripData.initialTrip` 和 `StarterTripData.seedMessages`。
- Chat 请求 `/api/chat` 成功时，应用后端返回的 `CanvasPatch`。
- 请求失败、网络不可用、服务器报错时，不修改 trip，追加一条明确的“Live Butler unavailable”消息。
- Explore/Tools 保留本地静态 starter 内容，不依赖网络也能打开。
- 翻译/汇率失败时显示不可用提示，不编造翻译来源或实时汇率。
- 本地状态保存到 `UserDefaults`，重启 App 后仍能看到上次 trip/messages/suggestions。

后续更完整的离线包可以拆成三层：

1. 小型 JSON 静态包：城市、常用短语、紧急电话、入境清单。
2. 图片/OCR 缓存：用户主动保存，避免自动占用太多空间。
3. 行程缓存：升级到 SwiftData 或 SQLite，支持多 trip、版本号、同步冲突处理。

## 8. 下一阶段建议

Phase 1：把当前 MVP 在模拟器跑稳，修 UI 细节和交互。
Phase 2：接 `/api/explore`、`/api/tools`、`/api/exchange-rate`，让 Explore/Tools 用真实后端数据。
Phase 3：接原生权限能力：相机 OCR、语音 STT/TTS、文件选择。
Phase 4：接地图 SDK、位置权限、路线/附近推荐。
Phase 5：接登录和云端 trip sync。
Phase 6：TestFlight、真机 QA、App Store 隐私表单和审核材料。

## 9. 本次 Issue #5 自查结论

- CanvasPatch 管道：已遵守。AI 生成的行程变更只来自 `POST /api/chat` 的 `patch`，并通过 `CanvasPatchApplier.apply` 合并。Explore 的 `Add to Plan` 不直接写 `TripDay`，只是把用户意图发回 Chat。
- 客户端密钥：没有 AI/第三方 Key。客户端只有后端 base URL。
- 离线行为：五个 Tab 都有本地 UI。Chat 失败时显示不可用消息；Trips 显示 starter canvas；Explore/Tools 显示本地静态内容；Me 显示本地缓存状态。
- 文档同步：本次未改变 `ARCHITECTURE.md`、`API_SPEC.md`、`MOBILE_STANDARD.md` 的契约，不需要同步修改这些文件。

## 10. 常见问题

如果 Chat 看起来没有连上：

1. 打开 App 后进入 `Chat`。
2. 输入一句：`Plan my first day in China`。
3. 如果后端成功，Butler 会返回新消息，并且可能更新 Trips 页面。
4. 如果后端失败，Chat 会显示 `Live Butler unavailable`，并保留原有 trip。
5. 先确认模拟器 Safari 能打开 `https://go2china.space`。
6. 如果 Safari 也打不开，检查 Mac 网络、代理、VPN、DNS。
7. 如果 Safari 能打开但 App 不行，把 `Live Butler unavailable` 那条消息截图发给开发者。

注意：iOS 端不会再显示连接状态条。连接结果只通过正常 Butler 回复或不可用消息体现。

如果字体看起来没生效：

1. 打开 macOS `Font Book`。
2. 临时安装 `ios/VisePandaIOS/Resources/Fonts` 里的字体。
3. 查看字体的 PostScript 名称。
4. 回到 `VisePandaTheme.swift`，把 `VPFont` 里的名称改成真实 PostScript 名称。

如果 Chat 一直显示 `Live Butler unavailable`：

1. 确认 Mac 能访问 `https://go2china.space`。
2. 确认 Vercel 后台 API 正常。
3. 确认 `/api/chat` 没有因为环境变量缺失报错。
4. 即使后端失败，iOS 也会保留本地 starter/static fallback，并且不会擅自改行程。

如果 Xcode 说没有模拟器：

1. 打开 Xcode。
2. 顶部菜单 `Window` → `Devices and Simulators`。
3. 进入 `Simulators`。
4. 添加一个 iPhone 模拟器。
5. 回到主窗口重新选择设备运行。
