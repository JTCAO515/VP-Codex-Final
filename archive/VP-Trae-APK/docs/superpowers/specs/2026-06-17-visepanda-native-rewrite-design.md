# VisePanda 原生重写设计

日期：2026-06-17

## 目标

从 0 开始重写 `VisePanda` 客户端，第一阶段以 Android 原生版本为核心交付物，建立一套稳定、可扩展、具备高完成度视觉体验的全新产品骨架，并为第二阶段的 iOS 原生版本复刻提供清晰的产品、设计和技术边界。

本次重写不是对旧工程做继续修补，而是重新定义信息架构、视觉系统、模块边界、数据流和稳定性策略，解决旧版本中协议适配脆弱、导航割裂、数据层耦合和 UI 质感不足的问题。

## 目标用户与产品定位

产品面向来华或计划来华的国际旅行者，核心价值不是“提供大量旅游信息”，而是成为一个具备品牌感和可信度的 AI 中国旅行顾问。

客户端的第一印象应当是：

- 高级而克制
- 有中国旅行品牌气质
- 不是廉价工具集合
- AI 规划是产品中心
- 浏览城市、探索路线、保存行程都是围绕“旅行决策”服务

## 技术路线结论

本次重写采用以下路线：

- 第一阶段：`Android 原生 + Kotlin + Jetpack Compose`
- 第二阶段：`iOS 原生 + SwiftUI`

明确不采用 Flutter 作为当前重写路线。

### 选择理由

当前项目目标以稳定性和可实现性为第一优先级，并接受“先 Android 后 iOS”的上线节奏。在这种前提下，直接选择 Flutter 虽然看似有双端代码复用优势，但会同时引入跨平台 UI、地图、聊天流、状态管理和工程体系的额外不确定性，不利于第一版高质量落地。

双原生分阶段更符合当前目标：

- Android 第一版更容易做扎实
- Compose 和 SwiftUI 在设计思想上相近，后续迁移成本可控
- 共享的是产品结构、接口契约和设计 token，而不是为了代码复用牺牲首版稳定性

## 产品结构

### 顶层导航

重写后的首版采用以下一级结构：

- `Home`
- `Explore`
- `Chat`
- `Trips`

`Tools` 不再作为底部一级导航，而被整合进首页内容区或次级入口。

### 结构取舍

旧版本把 `Map`、`Cities`、`Tools` 作为并列 tab，属于“按功能拆栏”的结构。新版本改为“按用户任务组织”的结构：

- `Home`：品牌首页与灵感入口
- `Explore`：浏览城市与地图探索
- `Chat`：AI 旅行规划主流程
- `Trips`：我的旅行资产

这样更符合用户实际路径：

1. 先被品牌和城市内容吸引
2. 浏览城市或在地图中探索
3. 进入 AI 规划
4. 保存和管理行程

## 视觉方向

本次重写采用用户确认的视觉路线：`A 东方奢雅`。

### 视觉关键词

- 深色底
- 温金点缀
- 墨黑、米金、玉石灰、竹青作为主辅配色
- 大留白
- 强图片感
- 国际化而非传统堆砌式中国风

### 视觉原则

目标是做出“东方奢雅，但不老气”的品牌感，不使用高饱和红金、复杂纹样堆叠或廉价玻璃拟态。页面气质应更接近高端旅行品牌或高完成度生活方式 App，而不是普通工具型旅游客户端。

### 设计系统约束

设计系统独立为单独模块，至少包含：

- Color tokens
- Typography scale
- Spacing scale
- Radius system
- Elevation / surface tokens
- Button / chip / card / section header / hero block / bottom nav 样式规范

这样 Android 与未来 iOS 都能共享同一套视觉语义，而不是只共享截图。

## 页面体验设计

### Home

`Home` 是品牌首页，不是功能列表页。

应包含：

- Hero 区：品牌标语、视觉主图、主 CTA
- AI 规划入口
- 精选城市卡片
- 主题灵感区，如 `First Time in China`、`Food Journey`、`Culture Route`
- 次级入口：Explore 与 Tools

首页的首要任务是建立品牌感和激发旅行欲望，其次才是导流到各功能模块。

### Explore

`Explore` 统一承载城市流和地图探索，不再把 `Cities` 与 `Map` 拆成两个完全分离的一级页面。

Explore 至少包含两种浏览形态：

- 城市卡片流
- 地图探索视图

用户可在同一模块内完成：

- 浏览城市
- 从地图点选城市
- 进入城市详情
- 再进入 Chat 做规划

### Chat

`Chat` 是产品核心，不是普通问答页。

Chat 页面应具备：

- 顶部当前目的地 / 行程上下文
- 高质量 prompt suggestion
- 流式消息渲染
- itinerary block、推荐卡片、图片、FAQ 等富内容容器
- 一键保存到 Trips

消息视觉不应采用普通 IM 聊天产品的廉价气质，而要更接近旅行顾问的对话体验。

### City Detail

城市详情是“目的地叙事页”，不是信息百科页。

至少包含：

- 强头图
- 一句话气质描述
- 推荐停留天数
- 预算感知
- Must-see / Must-eat / Stay / Tips
- 主 CTA：`Plan my trip to {City}`

### Trips

`Trips` 被定义为旅行资产中心，而不是简单的本地列表。

首版至少区分：

- 最近生成
- 已保存行程
- 空状态引导

未来可扩展：

- 草稿
- 最近打开
- 编辑与补充

### Tools

`Tools` 不再是单独主 tab，而是一个“旅行帮助中心”内容块，放在首页或二级页面中。

其视觉表达应与整站一致，采用场景化分类，而非简单图标矩阵。

建议分类：

- Payment
- Visa
- SIM / Internet
- Emergency
- Etiquette
- Useful Chinese

## 技术架构

### 总体结构

建议采用模块化原生架构：

- `app`
- `core/designsystem`
- `core/network`
- `core/common`
- `domain`
- `data`
- `feature/home`
- `feature/explore`
- `feature/chat`
- `feature/trips`
- `feature/tools`

### 各层职责

#### `app`

仅负责：

- Application
- NavHost 装配
- 依赖初始化
- 全局主题入口

#### `core/designsystem`

仅负责：

- 颜色、字体、间距、圆角等 token
- 复用 UI 组件
- 页面骨架与布局容器

#### `core/network`

仅负责：

- HTTP client
- SSE client
- 序列化
- 网络错误映射
- 统一响应解析

#### `domain`

仅负责业务用例，不依赖 Compose 页面。

示例：

- `SendChatMessageUseCase`
- `GetFeaturedDestinationsUseCase`
- `GetDestinationDetailUseCase`
- `GetMapDestinationsUseCase`
- `SaveTripUseCase`
- `DeleteTripUseCase`

#### `data`

仅负责：

- remote data source
- local data source
- repository 实现
- DTO 与 domain model 的映射

#### `feature/*`

仅负责：

- ViewModel
- UiState
- 页面事件
- Compose UI

页面层不直接解析 JSON，不直接操作低层网络协议。

## 数据域划分

### Destination Domain

将城市列表、城市详情、地图探索统一视为同一内容域。

核心组件：

- `DestinationRepository`
- `DestinationRemoteDataSource`
- `GetFeaturedDestinationsUseCase`
- `GetDestinationDetailUseCase`
- `GetMapDestinationsUseCase`

这样城市卡片、详情和地图 marker 使用同一套上游数据模型，避免旧项目中列表、地图、详情各自维护结构的割裂问题。

### Chat Domain

聊天流单独成域，不与普通 REST 混写。

核心组件：

- `ChatSseClient`
- `ChatRemoteDataSource`
- `ChatRepository`
- `SendChatMessageUseCase`

事件解析在协议层闭合，页面只消费结构化结果。

### Trip Domain

Trip 被定义为独立资产，而不是聊天附属产物。

首版至少支持：

- 保存
- 读取
- 删除
- 最近生成展示

未来可扩展：

- 草稿
- 编辑
- 最近打开
- 多来源导入

### Tools Domain

Tools 属于轻内容域，可以先以远程或本地配置的形式提供，但仍维持独立 repository 边界，避免混在首页静态文案里难以维护。

## 数据流设计

### UI 层约束

所有页面只消费统一 `UiState`：

- `Loading`
- `Success`
- `Empty`
- `Error`

页面层不直接 `try/catch` 网络异常，不直接分支协议细节。

### 错误模型

统一错误类型：

- `NetworkError`
- `ServerError`
- `ParseError`
- `EmptyData`
- `UnknownError`

这样错误提示、埋点、重试逻辑都能统一。

### 本地与远程分层

- 内容型数据：远程优先，允许轻缓存
- 用户资产型数据：本地优先，可靠持久化
- Chat 历史：首版按产品需要决定是否保存，避免无限膨胀

## 稳定性设计

这次重写的第一原则是稳定性，而不是“功能先跑起来再说”。

### 1. 聊天协议独立

SSE 是系统中最脆弱的链路之一，因此必须拥有独立协议层。后端事件结构变更时，改动范围应局限于协议与数据层，而不是扩散到 ViewModel 和 Compose 页面。

### 2. Mock / Real 双通道

为避免前端开发被后端或 CI 条件阻塞，第一天就保留 mock 通道。

允许 mock 的内容：

- 首页专题推荐
- Tools 部分内容
- Chat 的高级富卡片

首版必须真实打通的内容：

- 城市列表
- 城市详情
- 地图城市数据
- Chat 主链路
- Trip 本地保存

### 3. UI 只依赖稳定状态

页面只依赖 ViewModel 输出状态，不依赖底层协议行为，从根源上降低“一个协议细节导致整页异常”的风险。

### 4. 模块边界清晰

设计系统、网络、数据、页面、业务用例彼此分层。未来若做 iOS，对齐的是产品和设计契约，而不是拷贝 Android 特化实现。

## 第一阶段交付范围

第一阶段定义为：`Android 全量重写骨架版`。

该阶段不是 demo，而是后续持续迭代的新主线工程。

包含内容：

- 新工程骨架
- 新设计系统
- `Home / Explore / Chat / Trips` 顶层结构
- Tools 二级入口
- 高质量首页
- Explore 双视图
- City Detail
- Chat 主流程
- Trip 资产页
- mock / real 双通道
- 关键主链路真实打通

## 第二阶段交付范围

第二阶段定义为：`体验增强 + iOS 对齐准备`。

包含：

- Trip 详情与编辑
- 收藏城市 / 最近浏览
- Chat 历史恢复
- 更高级的 itinerary 富内容组件
- 更细致的动效和过渡
- 更完善的缓存策略
- 设计 token 文档化
- SwiftUI 对齐所需的页面规范与接口契约整理

## 实施顺序

建议按以下顺序实施：

1. 新工程骨架与设计系统
2. 顶层导航与页面容器
3. `Home + Explore`
4. `City Detail`
5. `Chat`
6. `Trips`
7. `Tools`
8. 文档、收尾与验证

这样可以尽早形成完整产品形态，同时把最复杂的聊天链路放在品牌骨架和浏览路径稳定之后接入，降低首轮实现风险。

## 测试与验证

首版验证至少覆盖：

- Chat SSE 事件解析
- 目的地数据映射
- Trip 保存 / 读取 / 删除
- 顶层导航链路
- Explore 视图切换
- 空状态 / 错态 / 重试态

建议测试分层：

- parser / mapper / usecase 单测
- ViewModel 状态测试
- 核心页面 smoke test

## 明确不做

为了确保第一阶段可实现且稳定，以下内容明确不在首版重写范围：

- Flutter 迁移
- Android 与 iOS 同时首发
- 复杂后台账号体系
- 过度离线化
- 一次性做完所有高级 itinerary 编辑功能
- 把所有营销内容都接成真实后端 CMS

## 成功标准

当第一阶段完成时，应满足：

- Android 客户端可作为新主线继续迭代
- 视觉上明显区别于旧版本，达到 `A 东方奢雅` 的高级感目标
- 产品结构清晰，围绕 `Home / Explore / Chat / Trips`
- 城市、地图、聊天、Trip 主链路真实可用
- 模块边界足够稳定，便于第二阶段增强和 iOS 对齐

## 最终结论

这次重写的核心不是“重新做一遍旧项目”，而是建立一个新的产品骨架：

- 技术上：Android 原生 Compose，未来 iOS 用 SwiftUI 对齐
- 产品上：按用户任务流重组，而不是按旧功能堆 tab
- 视觉上：采用 `A 东方奢雅`
- 工程上：把稳定性、模块边界和双阶段演进能力放在第一位

这套方案兼顾了当前最重要的三件事：

- 可实现性
- 稳定性
- 为双平台演进做准备
