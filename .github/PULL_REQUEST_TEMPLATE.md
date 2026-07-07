<!-- 端侧 Agent(Codex/Antigravity)提 PR 必须逐项填写本模板。缺项 = 驳回。 -->

## 对应 Issue

Closes #<!-- Issue 编号 -->

## 本次改动

<!-- 一两句话说清改了什么、为什么 -->

## 自查清单（必填，逐项勾选）

- [ ] 只修改了 Issue Scope 内的文件，未触碰 Do-not-touch 清单
- [ ] 未修改 `app/`、`lib/`、`components/`、`supabase/`、共享 md 文档、`package.json`/`VERSIONING.md`/`CHANGELOG.md`
- [ ] 构建通过（下方"验收证据"贴结果）：
  - Android：`./gradlew :app:testDebugUnitTest :app:assembleDebug`
  - iOS：Xcode build（`VisePandaIOS` scheme）
  - Web（仅架构师批准的例外）：`npm run build` + `npm run test`
- [ ] 断网验收完成：涉及页面在无网络下不崩溃、显示诚实降级提示、不显示假数据
- [ ] 无任何 API Key/密钥进入客户端代码或提交历史
- [ ] CanvasPatch 管道未被绕开（若使用本地写入白名单，在下方说明属于 `ARCHITECTURE.md` §4.2 哪一项）
- [ ] mock/static fallback 未删除
- [ ] 字段命名与 `API_SPEC.md` 一致

## 版本号

<!-- 填 Issue 预分配的版本号，例如 Android versionCode 15 / versionName "0.3.15"；iOS 同理。
     不得修改 package.json / VERSIONING.md / CHANGELOG.md（架构师合并后统一记录）。 -->

## 验收证据

<!-- 构建输出末尾几行 + 截图/录屏。涉及 UI 必须有截图；涉及离线路径必须有断网验收说明。 -->

## 架构文档影响

<!-- 本次改动是否需要架构师同步更新 ARCHITECTURE.md / API_SPEC.md / MOBILE_STANDARD.md？
     写"无"，或列出需要更新的点（由架构师最终落笔）。 -->
