# VisePanda Android Trae 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在新仓库 `visepanda-android-trae` 中完成安卓端第一轮最小可用修复，打通聊天、地图、城市入口和 Trip 保存闭环。

**Architecture:** 保留现有 `Compose + ViewModel + Repository` 结构，只对协议解析、导航连通、Trip 持久化和少量网络层收敛做定点修改。聊天流仍由 `SseClient` 驱动，普通 REST 请求继续走现有 repository，但修正与 web 后端不一致的解析逻辑。

**Tech Stack:** Kotlin, Jetpack Compose, Android ViewModel, StateFlow, OkHttp, DataStore, osmdroid, Gradle

---

## 文件结构

### 需要修改

- `app/build.gradle.kts`
  添加单元测试依赖，保证后续 repository / parser 逻辑可测。
- `app/src/main/java/space/jtcao/visepanda/data/api/SseClient.kt`
  修复 SSE 事件解析逻辑，使其适配 web 后端当前的 `message + JSON payload` 协议。
- `app/src/main/java/space/jtcao/visepanda/data/repository/MapRepository.kt`
  修复 `/api/map` 返回结构解析。
- `app/src/main/java/space/jtcao/visepanda/ui/chat/ChatViewModel.kt`
  接入 Trip 自动保存逻辑，尽量不扩大 UI 面改动。
- `app/src/main/java/space/jtcao/visepanda/ui/home/HomeScreen.kt`
  修复 “View All Cities” 入口回调。
- `app/src/main/java/space/jtcao/visepanda/ui/navigation/NavGraph.kt`
  继续向下传递导航回调，让详情页和 Trips 空状态页不再写死 TODO。
- `app/src/main/java/space/jtcao/visepanda/ui/cities/CityScreen.kt`
  让详情页 CTA 真正进入带城市上下文的聊天页。
- `app/src/main/java/space/jtcao/visepanda/ui/trips/TripsScreen.kt`
  让空状态按钮跳转聊天页。

### 需要新增

- `app/src/main/java/space/jtcao/visepanda/data/repository/TripAutoSave.kt`
  只负责判断聊天内容是否值得保存，以及生成 `Trip` 模型，避免把判断逻辑塞进 `ChatViewModel.kt`。
- `app/src/test/java/space/jtcao/visepanda/data/api/SseClientParserTest.kt`
  覆盖聊天事件解析。
- `app/src/test/java/space/jtcao/visepanda/data/repository/MapRepositoryParseTest.kt`
  覆盖地图对象结构解析。
- `app/src/test/java/space/jtcao/visepanda/data/repository/TripAutoSaveTest.kt`
  覆盖 Trip 自动保存规则。

---

### Task 1: 建立新仓库并补齐测试依赖

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/java/space/jtcao/visepanda/data/api/SseClientParserTest.kt`

- [ ] **Step 1: 创建新的工作副本并初始化 git**

Run:

```bash
cp -R /data/user/work/VisePanda-Android /data/user/work/visepanda-android-trae
cd /data/user/work/visepanda-android-trae
git remote remove origin || true
git checkout -b chore/bootstrap-trae
```

Expected: 新目录存在，分支切到 `chore/bootstrap-trae`。

- [ ] **Step 2: 为 `app/build.gradle.kts` 添加测试依赖**

把 `dependencies` 末尾补成下面这样：

```kotlin
    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
```

- [ ] **Step 3: 先写一个会失败的解析测试骨架**

在 `app/src/test/java/space/jtcao/visepanda/data/api/SseClientParserTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.api

import org.junit.Assert.assertTrue
import org.junit.Test
import space.jtcao.visepanda.data.model.ChatEvent

class SseClientParserTest {

    @Test
    fun `message event with token payload should become ChatEvent Token`() {
        val client = SseClient()
        val event = client.parseEventForTest(
            eventType = "message",
            data = """{"token":"Hello China"}"""
        )

        assertTrue(event is ChatEvent.Token)
    }
}
```

- [ ] **Step 4: 运行测试确认当前失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.api.SseClientParserTest"
```

Expected: FAIL，提示 `parseEventForTest` 不存在或事件解析结果不匹配。

- [ ] **Step 5: 提交基础准备**

```bash
git add app/build.gradle.kts app/src/test/java/space/jtcao/visepanda/data/api/SseClientParserTest.kt
git commit -m "test: add bootstrap test coverage for sse parser"
```

---

### Task 2: 修复聊天 SSE 协议解析

**Files:**
- Modify: `app/src/main/java/space/jtcao/visepanda/data/api/SseClient.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/data/api/SseClientParserTest.kt`

- [ ] **Step 1: 扩充失败测试，覆盖真实后端事件格式**

把测试文件补成：

```kotlin
package space.jtcao.visepanda.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import space.jtcao.visepanda.data.model.ChatEvent

class SseClientParserTest {

    private val client = SseClient()

    @Test
    fun `message event with token payload should become ChatEvent Token`() {
        val event = client.parseEventForTest("message", """{"token":"Hello China"}""")
        assertTrue(event is ChatEvent.Token)
        assertEquals("Hello China", (event as ChatEvent.Token).text)
    }

    @Test
    fun `message event with split payload should become ChatEvent Split`() {
        val event = client.parseEventForTest("message", """{"split":true}""")
        assertTrue(event is ChatEvent.Split)
    }

    @Test
    fun `message event with image payload should become ChatEvent Image`() {
        val event = client.parseEventForTest(
            "message",
            """{"image":{"key":"beijing","url":"/static/img/city-beijing.jpg","label":"Beijing"}}"""
        )
        assertTrue(event is ChatEvent.Image)
        event as ChatEvent.Image
        assertEquals("beijing", event.key)
        assertEquals("Beijing", event.label)
    }

    @Test
    fun `message event with faq payload should become ChatEvent Faq`() {
        val event = client.parseEventForTest(
            "message",
            """{"faq":{"id":"visa","title":"Visa Guide","icon":"🛂"}}"""
        )
        assertTrue(event is ChatEvent.Faq)
        assertEquals("visa", (event as ChatEvent.Faq).id)
    }

    @Test
    fun `done event should become ChatEvent Done`() {
        val event = client.parseEventForTest("done", """{"done":true}""")
        assertTrue(event is ChatEvent.Done)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.api.SseClientParserTest"
```

Expected: FAIL，当前 `SseClient.kt` 只支持 `token/split/image/faq/done/error` 作为顶层事件类型。

- [ ] **Step 3: 用最小改动修复 `SseClient.kt`**

把解析核心改成下面这种结构：

```kotlin
internal fun parseEventForTest(eventType: String, data: String): ChatEvent? {
    return parseEvent(eventType, data)
}

private fun parseEvent(eventType: String, data: String): ChatEvent? {
    if (data.isEmpty()) return null

    return when (eventType) {
        "message" -> parseMessagePayload(data)
        "done" -> ChatEvent.Done
        "error" -> ChatEvent.Error(
            try {
                json.parseToJsonElement(data).jsonObject["error"]?.jsonPrimitive?.content
                    ?: json.parseToJsonElement(data).jsonObject["message"]?.jsonPrimitive?.content
                    ?: data
            } catch (_: Exception) {
                data
            }
        )
        else -> null
    }
}

private fun parseMessagePayload(data: String): ChatEvent? {
    val obj = json.parseToJsonElement(data).jsonObject

    obj["token"]?.let {
        return ChatEvent.Token(it.jsonPrimitive.content)
    }
    if (obj["split"]?.jsonPrimitive?.booleanOrNull == true) {
        return ChatEvent.Split(true)
    }
    obj["image"]?.jsonObject?.let { image ->
        return ChatEvent.Image(
            key = image["key"]?.jsonPrimitive?.content ?: "",
            url = image["url"]?.jsonPrimitive?.content ?: "",
            label = image["label"]?.jsonPrimitive?.content ?: ""
        )
    }
    obj["faq"]?.jsonObject?.let { faq ->
        return ChatEvent.Faq(
            id = faq["id"]?.jsonPrimitive?.content ?: "",
            title = faq["title"]?.jsonPrimitive?.content ?: "",
            icon = faq["icon"]?.jsonPrimitive?.content ?: ""
        )
    }
    if (obj["done"]?.jsonPrimitive?.booleanOrNull == true) {
        return ChatEvent.Done
    }
    if (obj["error"] != null) {
        return ChatEvent.Error(obj["error"]?.jsonPrimitive?.content ?: "Unknown error")
    }
    return null
}
```

- [ ] **Step 4: 跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.api.SseClientParserTest"
```

Expected: PASS。

- [ ] **Step 5: 提交聊天协议修复**

```bash
git add app/src/main/java/space/jtcao/visepanda/data/api/SseClient.kt app/src/test/java/space/jtcao/visepanda/data/api/SseClientParserTest.kt
git commit -m "fix: align android sse parsing with web api"
```

---

### Task 3: 修复地图接口解析

**Files:**
- Modify: `app/src/main/java/space/jtcao/visepanda/data/repository/MapRepository.kt`
- Create: `app/src/test/java/space/jtcao/visepanda/data/repository/MapRepositoryParseTest.kt`

- [ ] **Step 1: 写失败测试，覆盖 web 后端真实返回结构**

在 `app/src/test/java/space/jtcao/visepanda/data/repository/MapRepositoryParseTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRepositoryParseTest {

    @Test
    fun `map response object should parse into marker list`() {
        val json = """
            {
              "cities": {
                "beijing": {"lat": 39.9042, "lng": 116.4074},
                "shanghai": {"lat": 31.2304, "lng": 121.4737}
              }
            }
        """.trimIndent()

        val markers = MapRepository.parseMarkersForTest(json)

        assertEquals(2, markers.size)
        assertEquals("beijing", markers[0].name)
        assertTrue(markers[0].lat > 0)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.repository.MapRepositoryParseTest"
```

Expected: FAIL，当前没有 `parseMarkersForTest`，且生产代码按数组解析。

- [ ] **Step 3: 实现对象结构解析**

把 `MapRepository.kt` 的核心改成：

```kotlin
class MapRepository {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMarkers(): List<MapMarker> {
        val url = URL("${ApiConfig.BASE_URL}/api/map")
        val response = url.readText()
        return parseMarkers(response)
    }

    companion object {
        fun parseMarkersForTest(raw: String): List<MapMarker> = parseMarkers(raw)

        private fun parseMarkers(raw: String): List<MapMarker> {
            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(raw).jsonObject
            val citiesObj = root["cities"]?.jsonObject ?: return emptyList()

            return citiesObj.entries.map { (name, element) ->
                val obj = element.jsonObject
                MapMarker(
                    name = name,
                    nameCn = name.replaceFirstChar { it.uppercase() },
                    lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    lng = obj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    vibe = "",
                    days = ""
                )
            }
        }
    }
}
```

- [ ] **Step 4: 跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.repository.MapRepositoryParseTest"
```

Expected: PASS。

- [ ] **Step 5: 提交地图解析修复**

```bash
git add app/src/main/java/space/jtcao/visepanda/data/repository/MapRepository.kt app/src/test/java/space/jtcao/visepanda/data/repository/MapRepositoryParseTest.kt
git commit -m "fix: parse object based map response"
```

---

### Task 4: 修复城市入口和关键导航

**Files:**
- Modify: `app/src/main/java/space/jtcao/visepanda/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/ui/navigation/NavGraph.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/ui/cities/CityScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/ui/trips/TripsScreen.kt`

- [ ] **Step 1: 先写待修改的接口形态**

目标接口如下：

```kotlin
fun HomeScreen(
    onCityClick: (String) -> Unit,
    onViewAllCities: () -> Unit,
    onStartChat: () -> Unit,
    viewModel: HomeViewModel = viewModel()
)

fun CityDetailScreen(
    cityName: String,
    onBack: () -> Unit,
    onPlanTrip: (String) -> Unit,
    viewModel: CityDetailViewModel = viewModel()
)

fun TripsScreen(
    onStartPlanning: () -> Unit,
    viewModel: TripsViewModel = viewModel()
)
```

- [ ] **Step 2: 改 `NavGraph.kt`，把导航能力往下传**

把相关路由块改成：

```kotlin
composable(Routes.HOME) {
    HomeScreen(
        onCityClick = { cityName ->
            navController.navigate(Routes.cityDetail(cityName))
        },
        onViewAllCities = {
            navController.navigate(Routes.CITIES)
        },
        onStartChat = {
            navController.navigate(Routes.CHAT)
        }
    )
}

composable(Routes.TRIPS) {
    TripsScreen(
        onStartPlanning = { navController.navigate(Routes.CHAT) }
    )
}

composable(
    route = Routes.CITY_DETAIL,
    arguments = listOf(navArgument("cityName") { type = NavType.StringType })
) { backStackEntry ->
    val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
    CityDetailScreen(
        cityName = cityName,
        onBack = { navController.popBackStack() },
        onPlanTrip = { city -> navController.navigate(Routes.chatCity(city)) }
    )
}
```

- [ ] **Step 3: 改 `HomeScreen.kt` / `CityScreen.kt` / `TripsScreen.kt`**

关键改动如下：

```kotlin
// HomeScreen.kt
Button(
    onClick = onViewAllCities,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp)
        .height(48.dp)
) {
    Text("View All Cities →")
}

// CityScreen.kt
Button(
    onClick = { onPlanTrip(cityName) },
    modifier = Modifier.fillMaxWidth().height(50.dp)
) {
    Text("💬 Plan a Trip to ${cityName.replaceFirstChar { it.uppercase() }}")
}

// TripsScreen.kt
Button(
    onClick = onStartPlanning,
    shape = RoundedCornerShape(12.dp)
) {
    Text("Start Planning →")
}
```

- [ ] **Step 4: 运行编译检查**

Run:

```bash
./gradlew compileDebugKotlin
```

Expected: PASS；若当前环境仍因 Gradle 下载失败，则至少确保 Kotlin 代码无明显签名不一致。

- [ ] **Step 5: 提交导航修复**

```bash
git add app/src/main/java/space/jtcao/visepanda/ui/home/HomeScreen.kt app/src/main/java/space/jtcao/visepanda/ui/navigation/NavGraph.kt app/src/main/java/space/jtcao/visepanda/ui/cities/CityScreen.kt app/src/main/java/space/jtcao/visepanda/ui/trips/TripsScreen.kt
git commit -m "fix: reconnect city and trip navigation flows"
```

---

### Task 5: 打通 Trip 自动保存闭环

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/data/repository/TripAutoSave.kt`
- Create: `app/src/test/java/space/jtcao/visepanda/data/repository/TripAutoSaveTest.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/ui/chat/ChatViewModel.kt`

- [ ] **Step 1: 写失败测试，明确自动保存规则**

在 `app/src/test/java/space/jtcao/visepanda/data/repository/TripAutoSaveTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TripAutoSaveTest {

    @Test
    fun `itinerary style content should produce trip`() {
        val content = """
            ### Trip Overview
            **Day 1: Forbidden City**
            - Morning: Visit Tiananmen
            **Day 2: Great Wall**
        """.trimIndent()

        val trip = TripAutoSave.createTripOrNull(city = "beijing", content = content)

        assertNotNull(trip)
        assertEquals("beijing", trip?.city)
    }

    @Test
    fun `generic answer should not produce trip`() {
        val trip = TripAutoSave.createTripOrNull(
            city = "beijing",
            content = "China has many high speed trains and mobile payments."
        )

        assertNull(trip)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.repository.TripAutoSaveTest"
```

Expected: FAIL，当前没有 `TripAutoSave`。

- [ ] **Step 3: 用独立文件实现保存规则**

在 `app/src/main/java/space/jtcao/visepanda/data/repository/TripAutoSave.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.repository

import space.jtcao.visepanda.data.model.Trip
import java.util.UUID

object TripAutoSave {

    fun createTripOrNull(city: String?, content: String): Trip? {
        val normalized = content.trim()
        if (normalized.isBlank()) return null

        val looksLikeTrip =
            Regex("""(?i)\bday\s*\d+\b""").containsMatchIn(normalized) ||
            normalized.contains("### Trip Overview") ||
            normalized.contains("Morning:") ||
            normalized.contains("Afternoon:") ||
            normalized.contains("Evening:")

        if (!looksLikeTrip) return null

        val detectedDays = Regex("""(?i)\bday\s*(\d+)\b""")
            .findAll(normalized)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .maxOrNull() ?: 0

        val safeCity = city?.takeIf { it.isNotBlank() } ?: "china"
        val title = if (detectedDays > 0) {
            "${safeCity.replaceFirstChar { it.uppercase() }} ${detectedDays}-day trip"
        } else {
            "${safeCity.replaceFirstChar { it.uppercase() }} trip"
        }

        return Trip(
            id = UUID.randomUUID().toString(),
            title = title,
            city = safeCity,
            days = detectedDays,
            content = normalized
        )
    }
}
```

- [ ] **Step 4: 在 `ChatViewModel.kt` 接入自动保存**

用 `AndroidViewModel` 挂应用上下文，并在完成流时保存：

```kotlin
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val tripRepository = TripRepository(application)
    private var currentCity: String? = null

    fun sendMessage(text: String, city: String? = null) {
        currentCity = city
        // 原有发送逻辑保持不变
    }

    private fun flushAccumulated(isSplit: Boolean) {
        if (accumulatedText.isNotEmpty() || accumulatedImages.isNotEmpty()) {
            val msg = ChatMessage(
                role = "assistant",
                content = accumulatedText,
                images = accumulatedImages.toList(),
                faqs = accumulatedFaqs.toList()
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + msg,
                currentStreamText = "",
                currentImages = emptyList(),
                currentFaqs = emptyList()
            )

            if (!isSplit) {
                maybeSaveTrip(msg.content)
            }

            accumulatedText = ""
            accumulatedImages.clear()
            accumulatedFaqs.clear()
        }
    }

    private fun maybeSaveTrip(content: String) {
        viewModelScope.launch {
            TripAutoSave.createTripOrNull(currentCity, content)?.let { trip ->
                tripRepository.saveTrip(trip)
            }
        }
    }
}
```

- [ ] **Step 5: 跑测试并提交**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.repository.TripAutoSaveTest"
```

Expected: PASS。

```bash
git add app/src/main/java/space/jtcao/visepanda/data/repository/TripAutoSave.kt app/src/main/java/space/jtcao/visepanda/ui/chat/ChatViewModel.kt app/src/test/java/space/jtcao/visepanda/data/repository/TripAutoSaveTest.kt
git commit -m "feat: auto save itinerary style chat results"
```

---

### Task 6: 做一轮代码收敛与最终验证

**Files:**
- Modify: `app/src/main/java/space/jtcao/visepanda/data/api/VisePandaApi.kt`
- Modify: `HANDOFF.md`

- [ ] **Step 1: 收敛明显无效的 API 声明**

若 `VisePandaApi.kt` 仍未被实际使用，则将其改成更诚实的最小注释版：

```kotlin
/**
 * 预留的 Retrofit 接口定义。
 *
 * 当前项目实际使用：
 * - 普通 REST：repository 中的 URL 直接请求
 * - SSE 聊天：SseClient
 *
 * 保留此文件仅作为未来统一网络层时的占位入口。
 */
interface VisePandaApi
```

如果你决定保留现状，也至少要把文件头注释改成和真实行为一致。

- [ ] **Step 2: 更新 `HANDOFF.md` 的真实状态**

把“已实现 / 已知问题 / Next Steps” 更新为更准确的描述，至少包含：

```md
- Chat SSE parsing aligned with current web API message payload format
- Map endpoint parsing now reads object-based cities response
- Home → All Cities navigation fixed
- City detail and empty trips CTAs now connect to chat
- Trip auto-save added for itinerary-style assistant responses
```

- [ ] **Step 3: 运行最终验证**

Run:

```bash
./gradlew testDebugUnitTest
```

如果网络允许，再运行：

```bash
./gradlew assembleDebug
```

Expected:

- `testDebugUnitTest` PASS
- `assembleDebug` PASS，或仅因外部 Gradle 下载问题失败

- [ ] **Step 4: 提交收尾改动**

```bash
git add app/src/main/java/space/jtcao/visepanda/data/api/VisePandaApi.kt HANDOFF.md
git commit -m "docs: align android handoff with implemented state"
```

- [ ] **Step 5: 关联远程仓库并推送**

如果环境已具备 GitHub 认证：

```bash
git remote add origin git@github.com:JTCAO515/visepanda-android-trae.git
git push -u origin HEAD
```

如果 SSH 不可用，再用 HTTPS：

```bash
git remote add origin https://github.com/JTCAO515/visepanda-android-trae.git
git push -u origin HEAD
```

Expected: 新仓库包含完整修复历史；如果认证失败，保留本地仓库并把失败原因回报给用户。

---

## 自检

### 规格覆盖

- SSE 协议修复：Task 2
- 地图解析修复：Task 3
- 城市与 Trips 导航修复：Task 4
- Trip 保存闭环：Task 5
- 代码收敛与 handoff 对齐：Task 6
- 新仓库创建：Task 1 + Task 6

没有遗漏设计文档中的核心要求。

### 占位检查

计划中没有使用 `TODO`、`TBD` 或“类似 Task N”的写法。每个任务都给出了具体文件、代码块和运行命令。

### 类型一致性

- `TripAutoSave.createTripOrNull()` 在计划中前后命名一致
- `parseEventForTest()` 与 `parseMarkersForTest()` 仅作为可测试入口，命名一致
- 导航回调 `onViewAllCities`、`onPlanTrip`、`onStartPlanning` 在计划中前后保持一致
