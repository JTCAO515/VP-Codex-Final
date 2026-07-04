# MOBILE_STANDARD.md — Android / iOS 移动端规范

适用范围：`android/`（Codex/Antigravity 负责）与 `ios/`（Codex 负责）。两端各自独立开发，禁止互相依赖对方代码，但必须遵守以下共同规范，保证产品行为、契约、约束一致。

## 1. 技术栈基线

| | Android | iOS |
|---|---|---|
| 语言/UI | Kotlin + Jetpack Compose + Material 3 | Swift + SwiftUI |
| DI | Hilt | 无强制要求 |
| 本地存储 | Room（trip/messages 缓存）+ DataStore（偏好） | UserDefaults（当前）；可升级 SQLite/SwiftData |
| 网络 | Retrofit + OkHttp | URLSession 或等价 |
| 后端 | `VISEPANDA_API_BASE_URL` 指向 Web `/api/*`（唯一后端，见 `API_SPEC.md`） | 同左 |

不允许引入与既有栈冲突的新框架（如 Android 引入 AppCompat 仅为语言切换 —— 已验证不需要，用自定义 `ContextWrapper` 即可，见 `DESIGN.md` ADR-118）。

## 2. Mock-first / 渐进替换纪律

- 每个新页面/新功能第一版必须 mock-first：本地静态数据 + 诚实的"暂无实时数据"提示，不接第三方真实 API（除非 Issue 明确要求）。
- 替换 mock 为真实数据源时，旧 fallback **必须保留**，作为网络失败/未配置时的优雅降级路径，不能删除。
- 参考模式：Android `LiveToolsRepository.fetchLiveRates()` —— `runCatching` 包裹真实请求，失败时保留静态数据 + 诚实文案，绝不崩溃、绝不伪造数据。

## 3. CanvasPatch 管道（强制）

- AI/Butler 产生的所有行程变更必须通过 `/api/chat` 返回的 `patch` 走 `CanvasPatchApplier` 合并本地状态，任何 ViewModel/Controller 都不允许直接拼装 `TripState`/`TripDay`。
- 允许绕开管道的"本地确定性写入"白名单：改名、alert 勾选、Explore Add to Trip、Day Detail 编辑描述/重排（见 `ARCHITECTURE.md` §4.2）。新增白名单项需先获得架构师批准并记录 ADR。

## 4. 离线与崩溃安全

- 每个新 Tab/新页面上线前必须验证：关闭 wifi + 移动数据后打开该页面，不崩溃，显示诚实的离线/不可用状态（不是空白，不是假数据）。
- 反序列化边界必须做 null 归一化：本地持久化格式升级后，旧数据缺字段时不能让"非空类型"字段在运行时是 null 后传播到业务逻辑（Android 参考 `TripJson.decodeTrip()` 的 `normalizeNulls()`）。
- 崩溃修复优先在数据边界（解码/序列化层）统一处理，不要在每个读取点零散打补丁。

## 5. 权限与安全

- 不做真实支付、不做相机/麦克风权限接入，除非 Issue 明确要求且已在 `ARCHITECTURE.md` 记录数据流。
- 客户端不得持有任何 AI/第三方服务 Key；唯一需要的配置是后端 base URL。
- 不得在客户端代码或提交历史里硬编码任何密钥/token。

## 6. 依赖与范围纪律

- 不新增第三方依赖，除非现有依赖确实无法满足需求，且已在 PR 描述中说明为什么必须新增、评估过的替代方案。
- 每个 PR 的 Scope 必须与对应 GitHub Issue 一致，不顺带做 Issue 之外的重构/新功能（"Do not touch" 清单是硬边界）。
- 双端字段/枚举命名必须与 Web 端 `lib/types/trip.ts`、`lib/tools/types.ts` 等权威定义 1:1 对应，不擅自改名或增减字段语义。

## 7. 构建与验收（PR 合并前必须满足）

- Android：`./gradlew :app:testDebugUnitTest :app:assembleDebug` 通过。
- iOS：Xcode build 通过（对应 scheme），单元测试通过（如有）。
- 涉及 UI 变化：附截图或模拟器验收说明。
- 涉及离线路径：附断网验收说明（见 §4）。

## 8. 文档同步

每个功能性 PR 合并后，提交者需要在 PR 描述里注明：本次改动是否需要同步更新 `ARCHITECTURE.md`/`API_SPEC.md`/`MOBILE_STANDARD.md`（由架构师最终确认并落笔），以及是否需要 `DESIGN.md` 新增 ADR。版本号/CHANGELOG 等交接类文档由各端 owner 自己维护（沿用现有 `HANDOFF.md`/`PLAN.md` 惯例）。
