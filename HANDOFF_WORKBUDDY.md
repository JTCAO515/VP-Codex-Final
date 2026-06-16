# vp-workbuddy — 项目交接文档 (WorkBuddy Handoff)

> **最后更新:** 2026-06-17 00:05
> **当前版本:** v0.2.1
> **仓库:** `https://github.com/JTCAO515/visepanda--android-workbuddy`
> **原始仓库:** `https://github.com/JTCAO515/VisePanda-Android`
> **Web 版后端:** `https://www.go2china.space` (Vercel, Python WSGI)
> **Release 下载:** https://github.com/JTCAO515/visepanda--android-workbuddy/releases

---

## 一、项目概述

### 这是什么？

**vp-workbuddy** 是一个 **AI 中国旅行伴侣 Android 原生应用**，帮助来华外国游客通过 AI 对话获取个性化旅行方案。

它是 [VisePanda Web 版](https://github.com/JTCAO515/vise-panda-2) 的原生 Android 重写，共享同一个后端 API (`go2china.space`)。

### 用户（JTCAO515）的需求

1. 将 Web 版 VisePanda 升级为原生 Android 体验
2. 保持与 Web 版功能对等（5 标签导航 + AI 聊天 + 36 城市 + 地图 + 行程 + 工具）
3. 应用名叫 **vp-workbuddy**，包名 `space.jtcao.vpworkbuddy`

### 技术栈

| 层 | 技术 |
|----|------|
| UI | Jetpack Compose + Material 3 |
| 语言 | Kotlin 1.9.22 |
| 导航 | Navigation Compose 2.7.7 |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 |
| 序列化 | kotlinx.serialization 1.6.3 |
| 图片 | Coil 2.6.0 |
| 地图 | osmdroid 6.1.18 (OpenStreetMap) |
| 存储 | DataStore Preferences |
| 构建 | Gradle 8.5 + JDK 17 |
| CI/CD | GitHub Actions |
| 后端 | go2china.space (DeepSeek V4 Flash, SSE 流式) |

---

## 二、项目状态总览

### 已完成 (WorkBuddy 本轮工作)

WorkBuddy 从原项目 VisePanda-Android v0.1.0 (13 commits, 构建通过但有多处问题) 开始，完成了以下工作：

| 阶段 | 提交 | 内容 |
|------|------|------|
| **克隆迁移** | — | 克隆原项目，推送到新仓库 visepanda--android-workbuddy |
| **P0 修复** | `7f75c25` | Release 签名、Retrofit 统一网络层、主线程 I/O 修复 |
| **P0+P1** | `0e0f022` | 聊天持久化、导航连接、Markdown 增强、图片渲染、JSON 安全 |
| **P2** | `90ced7e` | CI lint + 自动 versionCode、HANDOFF 更新 |
| **重命名** | `20f2dea` | 包名 → `space.jtcao.vpworkbuddy`、应用名 → `vp-workbuddy` |
| **构建修复** | `db96ac7` | ApiClient import 修复 + CityDetailContent 参数传递 |
| **崩溃修复** | `13a5128` | 移除 jakewharton converter，改用 ResponseBody 手动解析 |

### 当前功能完成度

| 模块 | 状态 | 说明 |
|------|------|------|
| Home | ✅ | 熊猫 Hero + 8 城市卡片 + 骨架屏 + 错误态 |
| Chat | ⚠️ | SSE 流式聊天可用，但后端有 `import re` 缺失 bug |
| Cities | ✅ | 36 城市网格 + 完整详情页（9 Section） |
| City Detail → Chat | ✅ | "Plan a Trip" 按钮已连接 |
| Map | ✅ | osmdroid 中国地图 + 36 城标记 + 34 城硬编码回退 |
| Trips | ✅ | DataStore 持久化 + CRUD + 空状态导航到 Chat |
| Tools | ✅ | 8 工具卡片网格 |
| Theme | ✅ | 熊猫中国风 + 暗色/亮色双主题 |
| CI/CD | ✅ | lint + 自动 versionCode + 构建 APK |

### 当前已知问题

| # | 严重度 | 问题 | 位置 |
|---|--------|------|------|
| 1 | 🔴 | **Chat 报错 `name 're' is not defined`** — 后端 Python 问题，`api/index.py` 缺少 `import re`。需要修改 Web 版后端 | go2china.space 后端 |
| 2 | 🟡 | Send 图标用了 deprecated 版本（应换 AutoMirrored） | ChatScreen.kt:573 |
| 3 | 🟡 | Chat 图标同样 deprecated | BottomNavItem.kt:21 |
| 4 | 🟡 | MapView 无生命周期管理（onPause/onResume） | MapScreen.kt |
| 5 | 🟢 | MarkdownText 不支持嵌套列表 | MarkdownText.kt |
| 6 | 🟢 | 部分 icon 用了 deprecated API | 多处 |
| 7 | 🟢 | 无单元测试 | — |

---

## 三、架构设计

### 整体架构

```
[go2china.space API (Vercel/DeepSeek)]
         ↑ ↓ HTTPS + SSE
[vp-workbuddy Android App]
  ├── ApiClient (Retrofit 单例, ResponseBody)
  ├── Repository 层 (手动 kotlinx.serialization 解析)
  ├── ViewModel 层 (MutableStateFlow + viewModelScope)
  └── UI 层 (Jetpack Compose + Navigation)
```

### 关键设计决策（WorkBuddy 做的）

| 决策 | 选择 | 原因 |
|------|------|------|
| 网络层 | Retrofit + ResponseBody 手动解析 | jakewharton converter 与 Kotlin 1.9 不兼容，改用 ResponseBody 零依赖 |
| 聊天持久化 | DataStore + kotlinx.serialization | 轻量，无需 Room。ChatMessage/ChatImage/ChatFaq 标记 @Serializable |
| JSON 安全 | buildJsonObject 替代字符串拼接 | 原 SseClient 用 escapeJson() 手拼 JSON，有注入风险 |
| 签名 | 独立 release keystore | 原项目用 debug 签名，无法上架。keystore 已提交到仓库（非生产最佳实践，但方便 CI） |
| 依赖注入 | 无（手动 new） | 项目规模小，未引入 Hilt/Koin。未来如需可加 |

### 为什么不引入 Hilt？

项目 36 个 Kotlin 文件、4131 行代码，引入 Hilt 会增加约 200KB APK + 大量样板代码（@Module/@Inject/@HiltViewModel 等），收益不大。当前 Repository 都是无状态的，ViewModel 在需要 Context 时用 AndroidViewModel（如 ChatViewModel）。

---

## 四、文件结构

```
visepanda--android-workbuddy/
├── .github/workflows/build.yml          — CI: lint → build → upload APK
├── app/
│   ├── build.gradle.kts                 — 应用构建配置
│   ├── proguard-rules.pro               — R8 混淆规则
│   ├── visepanda-release.keystore       — Release 签名密钥
│   └── src/main/java/space/jtcao/vpworkbuddy/
│       ├── MainActivity.kt              — 入口 Activity (Scaffold + NavGraph)
│       ├── VisePandaApp.kt              — Application (Coil 图片加载器配置)
│       ├── data/
│       │   ├── api/
│       │   │   ├── ApiConfig.kt         — BASE_URL + 超时配置
│       │   │   ├── ApiClient.kt         — Retrofit 单例 (ResponseBody, 无 converter)
│       │   │   ├── SseClient.kt         — OkHttp SSE 流解析 (196行, 核心)
│       │   │   └── VisePandaApi.kt      — Retrofit 接口定义
│       │   ├── model/
│       │   │   ├── ApiModels.kt         — MapMarker/MapApiResponse/CitiesResponse/ToolsResponse/AppConfig
│       │   │   ├── ChatEvent.kt         — ChatEvent sealed class + ChatMessage/ChatImage/ChatFaq
│       │   │   ├── City.kt              — City/CityDetail/PriceEstimate/FoodItem/HotelData/TipItem/MapData/PoiItem
│       │   │   ├── ToolData.kt          — ToolItem/ToolContent/ToolSection
│       │   │   └── Trip.kt              — Trip data class
│       │   └── repository/
│       │       ├── CityRepository.kt     — 城市列表 + 详情 (Retrofit + 手动解析)
│       │       ├── ChatRepository.kt     — 聊天 SSE 流代理
│       │       ├── MapRepository.kt      — 地图标记 (Retrofit + 手动解析)
│       │       ├── ToolsRepository.kt    — 工具列表 + 配置 (Retrofit + 手动解析)
│       │       └── TripRepository.kt     — DataStore 行程 CRUD
│       ├── ui/
│       │   ├── chat/
│       │   │   ├── ChatScreen.kt        — 聊天 UI (584行, 最大文件)
│       │   │   └── ChatViewModel.kt     — 聊天状态 + DataStore 持久化
│       │   ├── cities/
│       │   │   ├── CityScreen.kt        — 城市列表 + 详情 (423行)
│       │   │   └── CityViewModel.kt     — 城市数据加载
│       │   ├── components/
│       │   │   └── MarkdownText.kt      — 自定义 Markdown 渲染 (292行, 支持代码块/表格)
│       │   ├── home/
│       │   │   ├── HomeScreen.kt        — 首页 (454行)
│       │   │   └── HomeViewModel.kt     — 首页数据加载
│       │   ├── map/
│       │   │   ├── MapScreen.kt         — osmdroid 地图 (218行)
│       │   │   └── MapViewModel.kt      — 地图标记 + 34城回退
│       │   ├── navigation/
│       │   │   ├── BottomNavBar.kt      — 底部导航栏
│       │   │   ├── BottomNavItem.kt     — 导航项定义
│       │   │   ├── NavGraph.kt          — 导航图 (100行)
│       │   │   └── Routes.kt            — 路由常量
│       │   ├── theme/
│       │   │   ├── Color.kt             — 熊猫中国风色板
│       │   │   ├── Theme.kt             — Light/Dark 主题
│       │   │   └── Type.kt              — 字体定义
│       │   ├── tools/
│       │   │   ├── ToolsScreen.kt       — 工具箱 (138行)
│       │   │   └── ToolsViewModel.kt    — 工具数据加载
│       │   └── trips/
│       │       ├── TripsScreen.kt       — 行程列表 (235行)
│       │       └── TripsViewModel.kt    — 行程 CRUD
│       └── res/                         — 资源 (图标/字符串/颜色/主题)
├── build.gradle.kts                     — 根项目构建
├── settings.gradle.kts                  — 项目设置 (含腾讯镜像)
├── gradle.properties                    — Gradle 属性 (含 keystore 密码)
├── gradle/libs.versions.toml            — 版本目录
└── HANDOFF.md                           — 本文档
```

---

## 五、API 接口

所有接口通过 `https://www.go2china.space` (注意 `www` 前缀！旧域名 `go2china.space` 会 301 重定向)：

| 端点 | 方法 | 请求/响应 | Android 调用处 |
|------|------|----------|---------------|
| `/api/cities` | GET | `{ cities: { slug: City } }` | CityRepository |
| `/api/cities/{city}` | GET | `{ city: CityDetail }` | CityRepository |
| `/api/chat` | POST SSE | `{ messages, city? }` → SSE stream | SseClient |
| `/api/map` | GET | `{ cities: [MapMarker] }` | MapRepository |
| `/api/tools` | GET | `{ tools: { name: desc } }` | ToolsRepository |
| `/api/config` | GET | `{ version, map_center }` | ToolsRepository |

### SSE 事件类型

```
event: token   → data: "text chunk"
event: split   → data: {"boundary": true}
event: image   → data: {"key":"..","url":"..","label":".."}
event: faq     → data: {"id":"..","title":"..","icon":".."}
event: done    → data: ""
event: error   → data: {"message":".."}
```

---

## 六、关键代码逻辑

### 6.1 SSE 聊天流 (SseClient.kt)

```
用户输入 → ChatViewModel.sendMessage()
  → ChatRepository.streamChat()
    → SseClient.streamChat()
      → OkHttp POST /api/chat (callbackFlow)
        → 逐行解析 SSE (event:/data: 前缀)
          → ChatEvent.Token/Split/Image/Faq/Done/Error
            → ChatViewModel.handleEvent()
              → 累积 token → flush → ChatMessage 气泡
```

JSON 请求体构建使用 `buildJsonObject` (安全)，不再用字符串拼接。

### 6.2 网络请求 (Repository 模式)

```
Repository.getXXX()
  → withContext(Dispatchers.IO)
    → ApiClient.api.getXXX()  // Response<ResponseBody>
      → response.body()?.string()
        → Json.decodeFromString<T>(body)
```

所有网络请求都在 `Dispatchers.IO` 上执行，不会阻塞主线程。

### 6.3 聊天持久化 (ChatViewModel)

```
ChatViewModel (AndroidViewModel)
  → init { loadHistory() }
    → DataStore.data.map { CHAT_HISTORY_KEY }.first()
      → Json.decodeFromString<List<ChatMessage>>()
        → _uiState.value = ChatUiState(messages = saved)

每次 flushAccumulated() 后:
  → saveHistory()
    → Json.encodeToString(messages)
      → dataStore.edit { CHAT_HISTORY_KEY = encoded }
```

消息列表序列化为 JSON 字符串存入 DataStore。旋转屏幕/进程死亡后自动恢复。

### 6.4 Markdown 渲染 (MarkdownText.kt)

逐行解析，支持：
- `### Header` (1-3级)
- `**bold**`, `*italic*`, `` `code` ``
- `[text](url)` (显示为带下划线链接)
- `- unordered list`, `1. ordered list`
- `` ```code block``` `` (等宽字体 + 背景)
- `| table | syntax |` (表格渲染)
- `---` 分割线

---

## 七、构建和发布

### 本地构建

```bash
# 前置条件: JDK 17, Android SDK (platform 34, build-tools 34.0.0)

git clone https://github.com/JTCAO515/visepanda--android-workbuddy.git
cd visepanda--android-workbuddy

# 设置 SDK 路径
echo "sdk.dir=/path/to/android-sdk" > local.properties

# 构建
./gradlew assembleRelease
# APK → app/build/outputs/apk/release/app-release.apk
```

### CI 构建

GitHub Actions 自动触发 (push to master/main + 手动触发)：
1. **lint job** → 运行 lint 检查
2. **build job** → 自动递增 versionCode (基于 `github.run_number`) → 构建 → 上传 artifact

### Release 发布

当前最新 Release: **v0.2.1**
下载: https://github.com/JTCAO515/visepanda--android-workbuddy/releases

安装: `adb install vp-workbuddy-v0.2.1.apk`

---

## 八、WorkBuddy 的操作记录

### 完整操作流程

1. **理解项目**: 读取原仓库 HANDOFF.md + 浏览所有源码，生成分析报告
2. **创建新仓库**: 将 VisePanda-Android 克隆并推送到 visepanda--android-workbuddy
3. **P0 修复** (commit `7f75c25`):
   - 创建 `visepanda-release.keystore`
   - 创建 `ApiClient.kt` (Retrofit 单例)
   - 重写 CityRepository/MapRepository/ToolsRepository 使用 Retrofit
   - 重写 ToolsViewModel 使用 ToolsRepository
   - 移除 Composable 中的 CityRepository() 实例化
4. **P0+P1 修复** (commit `0e0f022`):
   - ChatViewModel → AndroidViewModel + DataStore 持久化
   - ChatMessage/ChatImage/ChatFaq 加 @Serializable
   - CityDetailScreen "Plan a Trip" → Chat(city) 导航
   - TripsScreen "Start Planning" → Chat 导航
   - Chat 图片用 AsyncImage (Coil) 渲染
   - MarkdownText 支持代码块和表格
   - SseClient.buildJsonBody 改用 buildJsonObject
5. **P2 修复** (commit `90ced7e`):
   - CI 添加 lint job
   - CI 自动 versionCode
   - 更新 HANDOFF.md
6. **重命名** (commit `20f2dea`):
   - 批量替换包名 `space.jtcao.visepanda` → `space.jtcao.vpworkbuddy`
   - 重命名源码目录
   - applicationId/namespace 更新
   - app_name → "vp-workbuddy"
   - versionName → "0.2.0"
7. **构建修复** (commit `db96ac7`):
   - 修复 ApiClient import 路径
   - 修复 CityDetailContent 缺少 onStartChat 参数
   - settings.gradle.kts 添加腾讯镜像
8. **崩溃修复** (commit `13a5128`):
   - 移除 `retrofit2-kotlinx-serialization-converter` (与 Kotlin 1.9 不兼容)
   - VisePandaApi 所有端点返回 `Response<ResponseBody>`
   - Repository 层手动 `Json.decodeFromString()`
   - 从 build.gradle.kts 移除 converter 依赖

### 遇到的错误和解决

| 错误 | 原因 | 解决 |
|------|------|------|
| `git push` 认证失败 | 缺少 GitHub token | 用户提供 PAT，gh auth login |
| Gradle init.gradle 语法错误 | URL 缺引号 + mavelCentral 拼写 | 修复 init.gradle |
| init.gradle 与 settings 冲突 | FAIL_ON_PROJECT_REPOS | 改为 settings.gradle.kts 中配置镜像 |
| 缺少 Android SDK | 环境无 SDK | 下载 cmdline-tools + sdkmanager 安装 platform 34 |
| `Class cannot be cast to ParameterizedType` | jakewharton converter 不兼容 Kotlin 1.9 | 移除 converter，改用 ResponseBody 手动解析 |
| `onStartChat` unresolved | CityDetailContent 参数未传递 | 添加参数并传递 |
| `asConverterFactory` unresolved | import 路径错误 | `retrofit2.converter` → `com.jakewharton.retrofit2.converter` |
| Chat `name 're' is not defined` | 后端 Python 缺少 `import re` | 未修复（需改 Web 版后端） |

---

## 九、给下一个 AI Agent 的建议

### 立即要做的事

1. **修复 Chat 问题** 🔴 — 这是最高优先级。需要去 Web 版仓库 `vise-panda-2` 的 `api/index.py` 添加 `import re`。Chat 功能是核心卖点，不能工作就毫无意义。

2. **测试验证** — 安装 v0.2.1 APK 到手机，逐个测试 5 个标签页的功能：
   - Home: 城市卡片是否加载、骨架屏是否正常
   - Chat: 修复后端后，SSE 流式是否正常
   - Map: 中国地图是否显示、标记是否可点击
   - Trips: 保存/删除是否正常
   - Tools: 工具卡片是否显示

### 中等优先级

3. **修复 deprecated API** — Send 图标、Chat 图标等换成 AutoMirrored 版本
4. **MapView 生命周期** — 在 DisposableEffect 中处理 onPause/onResume
5. **添加 ProGuard 规则** — 确保 osmdroid 和 kotlinx.serialization 在混淆后正常
6. **网络错误提示** — 离线时给用户友好提示

### 低优先级

7. **单元测试** — ChatViewModel 和 SseClient 的测试
8. **升级 Compose BOM** — 从 2024.02 升级，恢复 PullToRefreshBox
9. **MarkdownText 增强** — 支持嵌套列表、HTML 标签
10. **iOS 版本** — 如果用户需要

### 技术注意事项

- **不要重新引入 converter** — 当前 ResponseBody + 手动解析方案稳定可靠
- **DataStore 的 key 不要改** — `chat_messages` 是聊天持久化的 key，改了用户会丢历史
- **keystore 密码在 gradle.properties** — 如果要改，同步更新 build.gradle.kts
- **API 域名用 `www.go2china.space`** — 不带 www 会 301 重定向
- **腾讯镜像在 settings.gradle.kts** — 国内构建更快，海外构建也能用（会 fallback 到 google/mavenCentral）

---

## 十、快速参考

| 项目 | 值 |
|------|-----|
| 仓库 | https://github.com/JTCAO515/visepanda--android-workbuddy |
| Release | https://github.com/JTCAO515/visepanda--android-workbuddy/releases |
| 包名 | `space.jtcao.vpworkbuddy` |
| 应用名 | vp-workbuddy |
| 版本 | v0.2.1 |
| APK 大小 | ~2.5 MB |
| Kotlin 文件 | 36 个, ~4131 行 |
| 后端 | https://www.go2china.space |
| 后端源码 | https://github.com/JTCAO515/vise-panda-2 |
| minSdk | 24 (Android 7.0) |
| targetSdk | 34 |
| JDK | 17 |
| Gradle | 8.5 |
| 用户 | JTCAO515 |

---

## 十一、WorkBuddy 的思路总结

### 工作哲学

1. **先理解再动手** — 完整阅读 HANDOFF.md + 所有源码后才开始改
2. **优先级驱动** — P0 (安全/崩溃) → P1 (功能缺口) → P2 (质量)
3. **最小改动原则** — 每个 commit 聚焦一类问题，方便 review 和回滚
4. **遇到问题先诊断** — 不盲目重试，分析根因后再修复

### 关键判断

- **不用 Hilt**: 36 文件的小项目引入 DI 框架过度工程化
- **不用 Room**: DataStore 对聊天历史这种 KV 数据足够了
- **移除 converter 而非修复**: converter 的兼容性问题修复成本高（需要找兼容版本或自己写），不如直接用 ResponseBody
- **keystore 提交到仓库**: 对个人项目来说，方便 CI 构建 > 安全顾虑

---

*WorkBuddy 完成于 2026-06-17 00:05*
*下一个 Agent: 请从修复 Chat 的 `import re` 后端问题开始。祝顺利！🐼*
