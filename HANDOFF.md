# VisePanda-Android-Hermes — Fix Summary (v0.2.0)

> **Repo:** `github.com/JTCAO515/visepanda-android-hermes`
> **App name:** vp-hermes
> **APK:** GitHub Actions → Artifacts → `app-release.apk`

## 修复清单

### 核心数据链路
| # | 问题 | 文件 | 修复 |
|---|------|------|------|
| 1 | SSE 事件名不匹配 | `SseClient.kt` | 重写：后端发 `event: message` + payload `{"token/split/image/faq/error/done"}` → 真正确认后缀为 `event: message`（不是独立的 `event: token/split/image`） |
| 2 | 地图 dict 格式 | `MapRepository.kt` | 从 `citiesObj.entries.mapNotNull` 遍历，保留 fallback |
| 3 | CityDetail 缺 `name_en` | `City.kt` | `@SerialName("name_en")` 字段 |
| 4 | City 缺 `highlights` | `CityRepository.kt` | `jsonArray` 解析 |

### 导航修复
| # | 问题 | 文件 | 修复 |
|---|------|------|------|
| 5 | View All Cities 错位 | `HomeScreen.kt` + `NavGraph.kt` | 独立 `onViewAllCities` 回调 |
| 6 | CityDetail CTA 死按钮 | `CityScreen.kt` + `NavGraph.kt` | `onStartChat(cityName)` → `Routes.chatCity(city)` |
| 7 | Trips 空态死按钮 | `TripsScreen.kt` + `NavGraph.kt` | `onStartChat` → `Routes.CHAT` |

### 新功能
| # | 功能 | 描述 |
|---|------|------|
| 8 | Trip 自动保存 | ChatViewModel 检测 AI 回复含 "Day X/行程" 时自动调 TripRepository.saveTrip() |

### 配置
| # | 变更 | 文件 |
|---|------|------|
| 9 | App 名 → vp-hermes | `values/strings.xml` |
| — | Backend `import re` | `vise-panda-2/api/index.py` (已修，独立仓库) |

## 待验证
- [ ] GitHub Actions `assembleRelease` 构建通过
- [ ] 安装后聊天 SSE 流式渲染
- [ ] 地图显示真实城市标记
- [ ] View All Cities → 城市列表
- [ ] CityDetail "Start Planning" → 带城市上下文聊天
- [ ] Trips 空态 "Start Planning" → 聊天
