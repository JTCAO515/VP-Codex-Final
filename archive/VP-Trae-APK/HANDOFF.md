# VisePanda-Android Trae — Handoff Document

> **Last Updated:** 2026-06-17
> **Status:** 🧭 Rewrite core flow, Auth/Admin foundation, and Android Supabase email auth integration are in progress
> **Repo:** `visepanda-android-trae`（本地工作副本）
> **Live URL:** 原生 App — 无线上部署
> **Agent Memory Key:** VisePanda-Android

---

## Rewrite Track

- Main rewrite entry now starts from `VisePandaRewriteApp`
- Phase 1: design system, Home, Explore, City Detail, mock/real destination data
- Phase 2: Chat, Trips, and Tools now live under the rewrite feature tree
- Legacy `ui/*` screens still exist in the repo but are no longer the target architecture

---

## Auth & Admin Track

- Supabase schema and `user_profiles` migration added under `supabase/`
- `admin/` Next.js workspace bootstrapped with auth guard, users pages, and API routes
- Android rewrite flow now includes auth routes plus email login / register / forgot-password entry points
- Auth Phase 2 now includes real Android auth gateway shape, shared auth repository state, session restore hooks, account route/logout flow, admin dashboard, user filters, and recovery-link driven reset-password completion flow
- Phase validation target is `./gradlew testDebugUnitTest` and `cd admin && npm test`; Android verification may still be blocked by local Gradle/SDK environment

---

## 1. Product Overview

VisePanda 的 Android 原生客户端（Kotlin + Jetpack Compose），对应 Web 版 go2china.space。

功能对标 Web 版：智能城市导览、AI 旅行助手聊天、36 城市地图、行程管理、工具箱。

目标用户：计划来中国旅游的外国游客。

---

## 2. Architecture

```
[VisePanda Web API (go2china.space)]
         ↑ ↓ HTTPS / SSE
[VisePanda Android App]
  ├── API Layer (Retrofit + OkHttp SSE)
  ├── Data Layer (Repository 模式)
  ├── ViewModel (UI State MutableStateFlow)
  └── UI Layer (Jetpack Compose + Navigation)
```

**关键设计决策：**

| 决定 | 选择 | 原因 |
|------|------|------|
| 原生 vs TWA | 原生 Kotlin/Compose | 用户要求升级到原生体验 |
| 聊天协议 | OkHttp SSE 流式 | 匹配 Web 版 API，无需 WebSocket 基础设施 |
| 地图 | osmdroid (OpenStreetMap) | 免费，无需 API Key，中国数据完整 |
| 持久化 | DataStore | 轻量级 KV 存储，无需 Room/SQLite |
| 构建 | GitHub Actions | 云端编译，不依赖本地环境 |
| BOM 版本 | 2024.02.00 | 稳定版，但无 PullToRefreshBox（需 1.3+）|

---

## 3. Current State

### 已实现功能 ✅

- **Home Screen** — 熊猫品牌 Hero、8 城市卡片网格、骨架屏、错误态、手动刷新
- **Rewrite Chat Screen** — AI Travel Concierge flow on the new feature architecture
- **Rewrite Trips Screen** — trip assets loaded from the new rewrite flow
- **Rewrite Tools Screen** — travel help center entry integrated from Home
- **Chat Screen** — SSE 流式聊天、Markdown 文本渲染（bold/italic/code/link）、6 建议 FAQ Chip、输入栏
- **City List** — 36 城市 2 列网格、骨架屏、错误态
- **City Detail** — 完整的嵌套详情页（Header→Meta→BudgetTip→Highlights→PriceEstimate→Must-Eat→Accommodation→Tips→CTA）
- **Map Screen** — osmdroid 中国全览地图、36 金色圆形城市标记、点击弹出信息卡片、跳转详情
- **Trips Screen** — DataStore 本地持久化、行程卡片列表、空状态空插画、删除按钮
- **Tools Screen** — 8 工具卡片 🧳💰🛂💬🆘🛡️🏛️📚
- **Navigation** — Bottom Nav 5 Tab（Home/Chat/Map/Trips/Tools）+ CityDetail 路由
- **Theme** — 熊猫中国风色板（PandaAmber/BambooGreen/InkBlack），暗色/亮色模式
- **GitHub Actions CI/CD** — 全自动构建 Release APK，签名密钥内置
- **Chat SSE 协议修复** — 已适配 Web 后端当前的 `event: message + JSON payload`
- **Map 解析修复** — 已按 `/api/map` 的对象结构读取城市坐标
- **入口链路修复** — Home → All Cities、City Detail → Chat、Empty Trips → Chat 已打通
- **Trip 自动保存** — 行程型回答会自动保存到本地 `DataStore`

### 未实现 ❌

- **推送通知** — 未实现（Web 版也没有）
- **离线缓存** — 仅 Trip 有本地持久化
- **iOS 版本** — 未开始

### 已知问题 / 坑点 ⚠️

1. **PullToRefreshBox 不兼容** — Compose BOM 2024.02 不含此 API，所有下拉刷新已替换为手动刷新按钮
2. **GitHub Actions 下载超时** — 腾讯云服务器下载 GitHub Actions Artifact 超时（70s 只下了 1.3MB/2.2MB），需要在本地或有更好网络的机器下载
3. **MarkdownText 性能** — 自定义 AnnotatedString 渲染器对长文本有性能风险，但聊天消息通常较短
4. **Android SDK 缺失** — 当前接手环境缺少 Android SDK，导致 `testDebugUnitTest` / `assembleDebug` 无法在此处完成最终验证
5. **GitHub 凭据缺失** — 当前环境无法直接创建或推送 `visepanda-android-trae` 远程仓库

---

## 4. File Structure

```
VisePanda-Android/
├── .github/workflows/build.yml     — CI/CD: checkout → JDK 17 → gradle build → upload APK
├── app/
│   └── src/main/java/space/jtcao/visepanda/
│       ├── MainActivity.kt          — 入口 Activity + Scaffold + NavGraph
│       ├── VisePandaApp.kt          — Application 类（osmdroid 初始化）
│       ├── data/
│       │   ├── api/
│       │   │   ├── ApiConfig.kt     — BASE_URL = https://go2china.space
│       │   │   ├── SseClient.kt     — OkHttp SSE 逐行解析（7164字，核心）
│       │   │   └── VisePandaApi.kt  — Retrofit 接口定义
│       │   ├── model/
│       │   │   ├── ApiModels.kt     — MapMarker, MapApiResponse, AppConfig, MapCenter（共享模型）
│       │   │   ├── City.kt          — City, CityDetail, MapData, PoiItem 等
│       │   │   ├── ToolItem.kt      — ToolItem, ToolCategory
│       │   │   └── Trip.kt          — Trip data class 仅
│       │   └── repository/
│       │       ├── CityRepository.kt  — 城市列表 + 详情（含 map→Pair 适配）
│       │       ├── ChatRepository.kt  — 聊天 SSE 流（Flow 返回）
│       │       ├── MapRepository.kt   — 地图标记坐标
│       │       ├── ToolsRepository.kt — 工具列表
│       │       └── TripRepository.kt  — DataStore 本地行程 CRUD
│       ├── ui/
│       │   ├── home/HomeScreen.kt    — 首页（16457字，核心）
│       │   ├── chat/ChatScreen.kt    — 聊天页（20181字，核心）
│       │   ├── cities/CityScreen.kt  — 城市列表 + 城市详情（19027字）
│       │   ├── map/MapScreen.kt      — 地图 + 标记（9338字）
│       │   ├── trips/TripsScreen.kt  — 行程（8337字）
│       │   ├── tools/ToolsScreen.kt  — 工具箱（5319字）
│       │   ├── components/
│       │   │   └── MarkdownText.kt   — 自定义 Markdown 渲染器
│       │   ├── navigation/
│       │   │   ├── BottomNavBar.kt   — 底部导航栏
│       │   │   ├── Routes.kt         — 路由定义
│       │   │   └── NavGraph.kt       — 导航图
│       │   └── theme/                — 主题 (Color/Type/Theme)
│       └── res/
│           ├── drawable/             — Launcher icon (5 种分辨率 + 自适应 XML)
│           ├── mipmap-*/             — 启动图标
│           └── values/               — strings, colors, themes
├── build.gradle.kts                  — 项目级 Gradle
├── settings.gradle.kts               — 项目设置
├── gradle.properties                 — Gradle 属性
└── gradle/libs.versions.toml         — 版本目录
```

---

## 5. API / Interface

所有 API 端点通过 `https://go2china.space` 访问：

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/cities` | GET | 城市列表（map 格式，key=city name） |
| `/api/cities/{id}` | GET | 城市详情（嵌套 foods/hotels/tips/prices） |
| `/api/chat` | POST (SSE) | 聊天，请求 body: `{city, message, history}` → SSE 事件类型: token/split/image/faq/done/error |
| `/api/tools` | GET | 工具列表 |
| `/api/map` | GET | 地图标记坐标 |
| `/api/config` | GET | 应用配置 |

---

## 6. Key Config

```properties
# gradle.properties
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m
```

**签名密钥（内置于 CI）：**
- storePassword/alias/keyPassword 均在 build.gradle.kts 内写死
- 仅用于 Release APK 签名

**GitHub Actions Secret:** 无（签名密钥代码内嵌，已在公开仓库）

---

## 7. Core Logic / Data Flow

### 聊天 SSE 流
```
用户输入 → ChatViewModel.sendMessage() → ChatRepository.streamChat()
→ OkHttp POST /api/chat (body: {messages, city})
→ 逐行读取 SSE events → `event: message` + JSON payload
→ payload 解析成 token/split/image/faq/done/error
→ Flow<ChatEvent> → ChatViewModel → UI State → 流式渲染气泡
```

### 数据加载模式（所有模块统一）
```
Composable → viewModel.load()
→ Repository.getXXX()
→ ViewModel UI State (Loading → Success | Error)
→ 骨架屏 / 内容 / 错误态 + Retry
```

### 地图标记
```
MapScreen → MapViewModel → MapRepository.getMarkers()
→ 解析 `/api/map` 返回的 `{ cities: { cityName: { lat, lng } } }`
→ 失败时 fallback 到硬编码城市坐标
→ osmdroid MAPNIK 瓦片 + 金色圆形 Bitmap Marker
→ 点击 → CityInfoPopup → 跳转 CityDetailScreen
```

---

## 8. Frontend / UI Component Map

```
MainActivity
  └─ Scaffold
       ├─ BottomNavBar [Home|Chat|Map|Trips|Tools]
       └─ NavGraph
            ├─ HomeScreen → CityGrid (2列) → CityDetailScreen
            ├─ ChatScreen → MessageBubble / StreamBubble / InputBar
            ├─ CityListScreen → CityGrid (2列) → CityDetailScreen
            ├─ CityDetailScreen → Scrollable Detail Sections
            ├─ MapScreen → OSMChinaMap → CityInfoPopup → CityDetail
            ├─ TripsScreen → TripCard → Delete
            └─ ToolsScreen → ToolsGrid (2列卡片)
```

---

## 9. Dependencies

| 依赖 | 版本 | 用途 |
|------|------|------|
| AGP | 8.2.2 | Android Gradle Plugin |
| Kotlin | 1.9.22 | 主语言 |
| Compose BOM | 2024.02.00 | Compose 全家桶 |
| compose-navigation | - | 页面路由 |
| Retrofit | 2.9.0 | HTTP 客户端 |
| OkHttp | - | SSE 流式 + Retrofit 底层 |
| Coil | 2.5.0 | 图片加载 |
| osmdroid | 6.1.18 | OpenStreetMap 地图 |
| DataStore | - | 本地 KV 持久化 |
| Kotlin Serialization | - | JSON 序列化 |
| compileSdk | 34 | - |
| minSdk | 24 | Android 7.0+ |
| Gradle | 8.5 | 构建工具 |
| JDK | 17 (GitHub Actions) | - |

---

## 10. Next Steps

### 🔴 高优先级
1. **补 Android SDK 并跑本地验证** — 配置 `ANDROID_HOME` / `local.properties` 后运行 `testDebugUnitTest`、`assembleDebug`
2. **创建 GitHub 新仓库** — 在有凭据的环境中创建 `visepanda-android-trae` 并推送当前本地提交历史
3. **功能验证** — 安装后测试：首页加载、聊天 SSE 流、地图交互、详情页滚动、Trip 自动保存

### 🟡 中优先级
4. **如果 API 不通** — 检查 go2china.space 是否活着，CityRepository/PoiItem 等数据模型是否匹配实际 API 返回字段（有字段变动时可快速适配）
5. **地图中国瓦片** — 国内用户 osmdroid MAPNIK 可能慢，可换中国区瓦片源（如高德/天地图）

### 🟢 低优先级
6. **PullToRefreshBox** — 升级 Compose BOM 到 2024.09+，恢复下拉刷新交互
7. **行程 UI 增强** — 添加行程详情编辑页
8. **iOS 版本** — 如果用户需要

---

## 11. Troubleshooting

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| GitHub Actions 构建失败 | 见下方 | 参考修复日志 |
| 聊天没反应 | API 端点改变 / SSE 协议变 | 检查 VisePandaApi.kt + SseClient.kt |
| 地图不显示中国 | osmdroid 瓦片源受限 | 切换到天地图/高德瓦片 |
| APK 构建通过但安装闪退 | 签名问题 / minSdk 不兼容 | 用 `adb logcat` 看崩溃堆栈 |
| Markdown 渲染不正常 | 自定义解析器正则不覆盖所有格式 | 调试 MarkdownText.kt 的 `appendInlineStyled()` |

### 过往构建错误及修复记录

| 版本 | 错误 | 修复 |
|------|------|------|
| Step 1-7 | 连续 8 次构建失败 | 详见下文 |
| #27624862402 ✅ | duplicat MapMarker import → 移除，成功 | |
| #27624678476 ❌ | MapScreen.kt 文件损坏（行号前缀注入）+ ExperimentalMaterial3Api 缺失 | git restore + 修复 |
| #27624454855 ❌ | MapData 冲突 + MapScreen 无 MapMarker import + PullToRefreshBox + MarkdownText 各种编译错误 | 创建 ApiModels.kt，拆 Trip.kt，加 import，替换 PullToRefreshBox 为 Box |
| #27624138733 ❌ | MapData redeem + PullToRefreshBox + MarkdownText append 歧义 + dp.toInt + NavController 类型 | 7 个文件修复 |
| #27623853086 ❌ | Step 8 首次编译全量 Kotlin 错误 | 初始化修复 |

---

## 12. References

- **Web 版本:** go2china.space (Vercel)
- **Web 源码:** ~/projects/vise-panda-2 (git@github.com:JTCAO515/vise-panda-2.git)
- **技能:** `project-iteration` skill（迭代管理）
- **记忆关键词:** VisePanda-Android, Kotlin Compose, native APK
- **HANDOFF 状态:** 项目当前为 ⏸️ 已暂停

---

*End of Handoff.*

*如果恢复项目：加载 `project-iteration` skill，读 HANDOFF.md，然后检查 GitHub Actions 最新构建状态。*
