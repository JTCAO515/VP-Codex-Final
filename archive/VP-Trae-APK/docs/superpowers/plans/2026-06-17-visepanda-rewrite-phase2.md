# VisePanda Rewrite Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在新的重写主线上完成 `Chat + Trips + Tools` 的第一版可用实现，让 `Home / Explore / Destination / Chat / Trips` 形成完整产品闭环。

**Architecture:** Phase 2 不再扩展设计系统和目的地浏览骨架，而是聚焦三条用户主链路：AI 聊天、行程保存与回看、旅行帮助中心。实现上会保留 Phase 1 的 `feature/* + domain + data` 分层，新增独立的 chat domain、trip domain 和 tools domain；聊天仍采用 SSE，但协议层会在新的 feature 体系内被重新包裹，不直接复用旧 `ui/chat` 页面结构。

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, ViewModel, StateFlow, OkHttp, Kotlin Serialization, DataStore, JUnit4

---

## 文件结构

### 需要新增

- `app/src/main/java/space/jtcao/visepanda/domain/model/ChatMessageItem.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/model/ChatSuggestion.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/model/TripAsset.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/model/ToolEntry.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/repository/ChatRepository.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/repository/TripAssetRepository.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/repository/ToolRepository.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/StreamChatUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/SaveTripAssetUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetTripAssetsUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetToolEntriesUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/data/chat/ChatSseEvent.kt`
- `app/src/main/java/space/jtcao/visepanda/data/chat/ChatSseClient.kt`
- `app/src/main/java/space/jtcao/visepanda/data/chat/ChatRemoteDataSource.kt`
- `app/src/main/java/space/jtcao/visepanda/data/chat/ChatRepositoryImpl.kt`
- `app/src/main/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImpl.kt`
- `app/src/main/java/space/jtcao/visepanda/data/tools/ToolRepositoryImpl.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/trips/TripsUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/trips/TripsViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/trips/TripsScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/tools/ToolsUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/tools/ToolsViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/tools/ToolsScreen.kt`
- `app/src/test/java/space/jtcao/visepanda/data/chat/ChatSseClientTest.kt`
- `app/src/test/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImplTest.kt`
- `app/src/test/java/space/jtcao/visepanda/feature/chat/ChatViewModelTest.kt`

### 需要修改

- `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`
  将 `Chat` 和 `Trips` 从 placeholder 切换到新 feature 页面，并新增 `Tools` 的二级入口。
- `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`
  增加 `Tools` 或帮助中心入口。
- `HANDOFF.md`
  更新重写主线的 Phase 2 状态和新能力说明。

---

### Task 1: 建立 chat domain 与 SSE 协议解析层

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/domain/model/ChatMessageItem.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/model/ChatSuggestion.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/repository/ChatRepository.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/StreamChatUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/chat/ChatSseEvent.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/chat/ChatSseClient.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/data/chat/ChatSseClientTest.kt`

- [ ] **Step 1: 先写失败测试，固定 `message + JSON payload` 的 SSE 解析**

在 `app/src/test/java/space/jtcao/visepanda/data/chat/ChatSseClientTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSseClientTest {

    @Test
    fun `message event with token payload should become token event`() {
        val client = ChatSseClient()
        val event = client.parseForTest(
            eventType = "message",
            data = """{"token":"Hello China"}"""
        )

        assertTrue(event is ChatSseEvent.Token)
        assertEquals("Hello China", (event as ChatSseEvent.Token).text)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.chat.ChatSseClientTest"
```

Expected: FAIL，`ChatSseClient` / `ChatSseEvent` 尚不存在。

- [ ] **Step 3: 实现最小 chat domain 与 SSE 事件模型**

创建 `ChatMessageItem.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class ChatMessageItem(
    val id: String,
    val role: String,
    val content: String,
    val cityId: String? = null
)
```

创建 `ChatSuggestion.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class ChatSuggestion(
    val id: String,
    val title: String,
    val prompt: String
)
```

创建 `ChatRepository.kt`：

```kotlin
package space.jtcao.visepanda.domain.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem

interface ChatRepository {
    fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent>
}
```

创建 `StreamChatUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.repository.ChatRepository

class StreamChatUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent> = repository.stream(cityId, history, prompt)
}
```

创建 `ChatSseEvent.kt`：

```kotlin
package space.jtcao.visepanda.data.chat

sealed interface ChatSseEvent {
    data class Token(val text: String) : ChatSseEvent
    data object Split : ChatSseEvent
    data class Image(val key: String, val url: String, val label: String) : ChatSseEvent
    data class Faq(val id: String, val title: String, val icon: String) : ChatSseEvent
    data object Done : ChatSseEvent
    data class Error(val message: String) : ChatSseEvent
}
```

创建 `ChatSseClient.kt`：

```kotlin
package space.jtcao.visepanda.data.chat

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatSseClient {

    private val json = Json { ignoreUnknownKeys = true }

    internal fun parseForTest(eventType: String, data: String): ChatSseEvent? {
        return parseEvent(eventType, data)
    }

    private fun parseEvent(eventType: String, data: String): ChatSseEvent? {
        if (data.isBlank()) return null

        return when (eventType) {
            "message" -> parseMessagePayload(data)
            "done" -> ChatSseEvent.Done
            "error" -> ChatSseEvent.Error(data)
            else -> null
        }
    }

    private fun parseMessagePayload(data: String): ChatSseEvent? {
        val obj = json.parseToJsonElement(data).jsonObject

        obj["token"]?.let {
            return ChatSseEvent.Token(it.jsonPrimitive.content)
        }
        if (obj["split"]?.jsonPrimitive?.booleanOrNull == true) {
            return ChatSseEvent.Split
        }
        obj["image"]?.jsonObject?.let { image ->
            return ChatSseEvent.Image(
                key = image["key"]?.jsonPrimitive?.content ?: "",
                url = image["url"]?.jsonPrimitive?.content ?: "",
                label = image["label"]?.jsonPrimitive?.content ?: ""
            )
        }
        obj["faq"]?.jsonObject?.let { faq ->
            return ChatSseEvent.Faq(
                id = faq["id"]?.jsonPrimitive?.content ?: "",
                title = faq["title"]?.jsonPrimitive?.content ?: "",
                icon = faq["icon"]?.jsonPrimitive?.content ?: ""
            )
        }
        if (obj["done"]?.jsonPrimitive?.booleanOrNull == true) {
            return ChatSseEvent.Done
        }
        if (obj["error"] != null) {
            return ChatSseEvent.Error(obj["error"]?.jsonPrimitive?.content ?: "Unknown error")
        }
        return null
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.chat.ChatSseClientTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 chat domain 与协议层**

```bash
git add app/src/main/java/space/jtcao/visepanda/domain/model/ChatMessageItem.kt app/src/main/java/space/jtcao/visepanda/domain/model/ChatSuggestion.kt app/src/main/java/space/jtcao/visepanda/domain/repository/ChatRepository.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/StreamChatUseCase.kt app/src/main/java/space/jtcao/visepanda/data/chat app/src/test/java/space/jtcao/visepanda/data/chat/ChatSseClientTest.kt
git commit -m "feat: add rewrite chat domain and sse parser"
```

---

### Task 2: 建立 chat remote data source 与 repository 实现

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/data/chat/ChatRemoteDataSource.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/chat/ChatRepositoryImpl.kt`

- [ ] **Step 1: 写失败测试，固定仓储转发行为**

在 `app/src/test/java/space/jtcao/visepanda/data/chat/ChatRepositoryImplTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.chat

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import space.jtcao.visepanda.domain.model.ChatMessageItem

class ChatRepositoryImplTest {

    @Test
    fun `stream should delegate to remote data source`() = runBlocking {
        val repo = ChatRepositoryImpl(
            remote = object : ChatRemoteDataSource {
                override fun stream(cityId: String?, history: List<ChatMessageItem>, prompt: String) =
                    flowOf(ChatSseEvent.Token("delegated"))
            }
        )

        val event = repo.stream(null, emptyList(), "Hello").first()

        assertTrue(event is ChatSseEvent.Token)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.chat.ChatRepositoryImplTest"
```

Expected: FAIL，`ChatRemoteDataSource` / `ChatRepositoryImpl` 尚不存在或接口不匹配。

- [ ] **Step 3: 实现最小 remote data source 与仓储**

创建 `ChatRemoteDataSource.kt`：

```kotlin
package space.jtcao.visepanda.data.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.jtcao.visepanda.data.repository.ChatRepository as LegacyChatRepository
import space.jtcao.visepanda.domain.model.ChatMessageItem

interface ChatRemoteDataSource {
    fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent>
}

class ChatRemoteDataSourceImpl(
    private val legacyRepository: LegacyChatRepository = LegacyChatRepository()
) : ChatRemoteDataSource {
    override fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent> = flow {
        legacyRepository.streamChat(cityId, prompt, emptyList()).collect { event ->
            when (event) {
                is space.jtcao.visepanda.data.model.ChatEvent.Token -> emit(ChatSseEvent.Token(event.text))
                is space.jtcao.visepanda.data.model.ChatEvent.Split -> emit(ChatSseEvent.Split)
                is space.jtcao.visepanda.data.model.ChatEvent.Image -> emit(ChatSseEvent.Image(event.key, event.url, event.label))
                is space.jtcao.visepanda.data.model.ChatEvent.Faq -> emit(ChatSseEvent.Faq(event.id, event.title, event.icon))
                is space.jtcao.visepanda.data.model.ChatEvent.Done -> emit(ChatSseEvent.Done)
                is space.jtcao.visepanda.data.model.ChatEvent.Error -> emit(ChatSseEvent.Error(event.message))
            }
        }
    }
}
```

创建 `ChatRepositoryImpl.kt`：

```kotlin
package space.jtcao.visepanda.data.chat

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val remote: ChatRemoteDataSource
) : ChatRepository {
    override fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent> = remote.stream(cityId, history, prompt)
}
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.chat.ChatRepositoryImplTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 chat repository 实现**

```bash
git add app/src/main/java/space/jtcao/visepanda/data/chat/ChatRemoteDataSource.kt app/src/main/java/space/jtcao/visepanda/data/chat/ChatRepositoryImpl.kt app/src/test/java/space/jtcao/visepanda/data/chat/ChatRepositoryImplTest.kt
git commit -m "feat: add rewrite chat repository"
```

---

### Task 3: 完成新的 Chat 页面与状态流

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatUiState.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatViewModel.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatScreen.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/feature/chat/ChatViewModelTest.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`

- [ ] **Step 1: 写失败测试，固定发送消息后的状态变化**

在 `app/src/test/java/space/jtcao/visepanda/feature/chat/ChatViewModelTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.feature.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.repository.ChatRepository
import space.jtcao.visepanda.domain.usecase.StreamChatUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `send should append streamed assistant text`() = runTest {
        val repo = object : ChatRepository {
            override fun stream(cityId: String?, history: List<space.jtcao.visepanda.domain.model.ChatMessageItem>, prompt: String) =
                flow {
                    emit(ChatSseEvent.Token("Hello"))
                    emit(ChatSseEvent.Token(" China"))
                    emit(ChatSseEvent.Done)
                }
        }

        val viewModel = ChatViewModel(StreamChatUseCase(repo))
        viewModel.send("Plan Shanghai", "shanghai")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any { it.role == "assistant" && it.content.contains("Hello China") })
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.chat.ChatViewModelTest"
```

Expected: FAIL，`ChatViewModel` / `ChatUiState` 尚不存在。

- [ ] **Step 3: 实现最小 chat feature**

创建 `ChatUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.chat

import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.model.ChatSuggestion

data class ChatUiState(
    val cityId: String? = null,
    val input: String = "",
    val isStreaming: Boolean = false,
    val messages: List<ChatMessageItem> = emptyList(),
    val suggestions: List<ChatSuggestion> = listOf(
        ChatSuggestion("first-time", "First time in China", "Plan a 5-day first trip to China."),
        ChatSuggestion("food", "Food route", "Design a food-focused trip in Chengdu and Shanghai.")
    ),
    val error: String? = null
)
```

创建 `ChatViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.chat.ChatRemoteDataSourceImpl
import space.jtcao.visepanda.data.chat.ChatRepositoryImpl
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.usecase.StreamChatUseCase

class ChatViewModel(
    private val streamChat: StreamChatUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun updateInput(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun send(prompt: String, cityId: String? = _uiState.value.cityId) {
        if (prompt.isBlank()) return

        val userMessage = ChatMessageItem(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = prompt,
            cityId = cityId
        )

        _uiState.value = _uiState.value.copy(
            cityId = cityId,
            input = "",
            error = null,
            isStreaming = true,
            messages = _uiState.value.messages + userMessage
        )

        viewModelScope.launch {
            val assistantBuffer = StringBuilder()

            streamChat(cityId, _uiState.value.messages, prompt).collect { event ->
                when (event) {
                    is ChatSseEvent.Token -> assistantBuffer.append(event.text)
                    is ChatSseEvent.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            error = event.message
                        )
                    }
                    is ChatSseEvent.Done -> {
                        val assistant = ChatMessageItem(
                            id = UUID.randomUUID().toString(),
                            role = "assistant",
                            content = assistantBuffer.toString(),
                            cityId = cityId
                        )
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            messages = _uiState.value.messages + assistant
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ChatViewModel(
                    streamChat = StreamChatUseCase(
                        ChatRepositoryImpl(
                            remote = ChatRemoteDataSourceImpl()
                        )
                    )
                )
            }
        }
    }
}
```

创建 `ChatScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun ChatScreen(
    cityId: String? = null,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "AI Travel Concierge",
            subtitle = cityId?.let { "Planning for ${it.replaceFirstChar(Char::titlecase)}" }
                ?: "Elegant planning for your next China trip"
        )

        if (state.messages.isEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.suggestions.forEach { suggestion ->
                    Button(onClick = { viewModel.send(suggestion.prompt, cityId) }) {
                        Text(suggestion.title)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                Text(
                    text = "${message.role.uppercase()}: ${message.content}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = state.input,
            onValueChange = viewModel::updateInput,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ask VisePanda") }
        )

        Button(
            onClick = { viewModel.send(state.input, cityId) },
            enabled = !state.isStreaming && state.input.isNotBlank()
        ) {
            Text(if (state.isStreaming) "Streaming..." else "Send")
        }
    }
}
```

把 `RewriteNavHost.kt` 中 `Chat` 路由替换为：

```kotlin
composable(RewriteRoutes.CHAT) {
    ChatScreen()
}
```

并把 detail 页中的：

```kotlin
onPlanTrip = {
    navController.navigate(RewriteRoutes.CHAT) {
        launchSingleTop = true
    }
}
```

改成：

```kotlin
onPlanTrip = { cityId ->
    navController.navigate(RewriteRoutes.CHAT) {
        launchSingleTop = true
    }
}
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.chat.ChatViewModelTest"
```

Expected: PASS。

- [ ] **Step 5: 提交新的 chat flow**

```bash
git add app/src/main/java/space/jtcao/visepanda/feature/chat app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt app/src/test/java/space/jtcao/visepanda/feature/chat/ChatViewModelTest.kt
git commit -m "feat: add rewrite chat flow"
```

---

### Task 4: 建立 trip asset domain 与 DataStore 映射

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/domain/model/TripAsset.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/repository/TripAssetRepository.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/SaveTripAssetUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetTripAssetsUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImpl.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImplTest.kt`

- [ ] **Step 1: 写失败测试，固定 trip mapper 行为**

在 `app/src/test/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImplTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.trip

import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.data.model.Trip

class TripAssetRepositoryImplTest {

    @Test
    fun `legacy trip should map into trip asset`() {
        val legacy = Trip(
            id = "1",
            title = "Shanghai 3-day trip",
            city = "shanghai",
            days = 3,
            content = "Day 1..."
        )

        val asset = legacy.toTripAsset()

        assertEquals("1", asset.id)
        assertEquals("shanghai", asset.cityId)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.trip.TripAssetRepositoryImplTest"
```

Expected: FAIL，`TripAsset` / `toTripAsset` 尚不存在。

- [ ] **Step 3: 实现 trip asset domain 与 repository**

创建 `TripAsset.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class TripAsset(
    val id: String,
    val title: String,
    val cityId: String,
    val days: Int,
    val content: String
)
```

创建 `TripAssetRepository.kt`：

```kotlin
package space.jtcao.visepanda.domain.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.TripAsset

interface TripAssetRepository {
    suspend fun save(asset: TripAsset)
    fun observe(): Flow<List<TripAsset>>
    suspend fun delete(id: String)
}
```

创建 `SaveTripAssetUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.TripAssetRepository

class SaveTripAssetUseCase(
    private val repository: TripAssetRepository
) {
    suspend operator fun invoke(asset: TripAsset) = repository.save(asset)
}
```

创建 `GetTripAssetsUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.TripAssetRepository

class GetTripAssetsUseCase(
    private val repository: TripAssetRepository
) {
    operator fun invoke(): Flow<List<TripAsset>> = repository.observe()
}
```

创建 `TripAssetRepositoryImpl.kt`：

```kotlin
package space.jtcao.visepanda.data.trip

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.jtcao.visepanda.data.model.Trip
import space.jtcao.visepanda.data.repository.TripRepository
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.TripAssetRepository

class TripAssetRepositoryImpl(
    private val legacyRepository: TripRepository
) : TripAssetRepository {

    override suspend fun save(asset: TripAsset) {
        legacyRepository.saveTrip(asset.toLegacyTrip())
    }

    override fun observe(): Flow<List<TripAsset>> =
        legacyRepository.getTrips().map { trips -> trips.map { it.toTripAsset() } }

    override suspend fun delete(id: String) {
        legacyRepository.deleteTrip(id)
    }
}

fun Trip.toTripAsset(): TripAsset =
    TripAsset(
        id = id,
        title = title,
        cityId = city,
        days = days,
        content = content
    )

fun TripAsset.toLegacyTrip(): Trip =
    Trip(
        id = id,
        title = title,
        city = cityId,
        days = days,
        content = content
    )
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.trip.TripAssetRepositoryImplTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 trip asset 层**

```bash
git add app/src/main/java/space/jtcao/visepanda/domain/model/TripAsset.kt app/src/main/java/space/jtcao/visepanda/domain/repository/TripAssetRepository.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/SaveTripAssetUseCase.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/GetTripAssetsUseCase.kt app/src/main/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImpl.kt app/src/test/java/space/jtcao/visepanda/data/trip/TripAssetRepositoryImplTest.kt
git commit -m "feat: add rewrite trip asset layer"
```

---

### Task 5: 完成 Trips 页面与保存闭环

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/feature/trips/TripsUiState.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/trips/TripsViewModel.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/trips/TripsScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/chat/ChatViewModel.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`

- [ ] **Step 1: 写失败测试，固定聊天完成后的自动保存规则**

在 `app/src/test/java/space/jtcao/visepanda/feature/chat/ChatTripSaveTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.feature.chat

import org.junit.Assert.assertNotNull
import org.junit.Test
import space.jtcao.visepanda.data.repository.TripAutoSave

class ChatTripSaveTest {

    @Test
    fun `itinerary content should produce trip asset candidate`() {
        val trip = TripAutoSave.createTripOrNull(
            city = "shanghai",
            content = """
                ### Trip Overview
                **Day 1: The Bund**
                **Day 2: French Concession**
            """.trimIndent()
        )

        assertNotNull(trip)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.chat.ChatTripSaveTest"
```

Expected: FAIL，若包路径不匹配或类尚未被新的 feature 层接入。

- [ ] **Step 3: 实现新的 Trips feature 与聊天保存闭环**

创建 `TripsUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.trips

import space.jtcao.visepanda.domain.model.TripAsset

sealed interface TripsUiState {
    data object Loading : TripsUiState
    data class Success(val trips: List<TripAsset>) : TripsUiState
    data class Error(val message: String) : TripsUiState
}
```

创建 `TripsViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.repository.TripRepository
import space.jtcao.visepanda.data.trip.TripAssetRepositoryImpl
import space.jtcao.visepanda.domain.usecase.GetTripAssetsUseCase

class TripsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TripAssetRepositoryImpl(TripRepository(application))
    private val getTripAssets = GetTripAssetsUseCase(repository)

    private val _uiState = MutableStateFlow<TripsUiState>(TripsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            getTripAssets().collect { trips ->
                _uiState.value = TripsUiState.Success(trips)
            }
        }
    }
}
```

创建 `TripsScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun TripsScreen(
    onStartPlanning: () -> Unit,
    viewModel: TripsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val uiState = state) {
        TripsUiState.Loading -> Text("Loading trips...")
        is TripsUiState.Error -> Text(uiState.message)
        is TripsUiState.Success -> {
            if (uiState.trips.isEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VpSectionHeader(
                        title = "Saved Trips",
                        subtitle = "Your travel assets will live here."
                    )
                    Button(onClick = onStartPlanning) { Text("Plan with AI") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.trips, key = { it.id }) { trip ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(trip.title, style = MaterialTheme.typography.titleLarge)
                            Text(trip.content, maxLines = 4, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
```

把 `ChatViewModel.kt` 增加 trip asset 保存：

```kotlin
private fun maybeSaveTrip(cityId: String?, content: String) {
    viewModelScope.launch {
        val legacy = space.jtcao.visepanda.data.repository.TripAutoSave.createTripOrNull(cityId, content) ?: return@launch
        val repository = space.jtcao.visepanda.data.trip.TripAssetRepositoryImpl(
            space.jtcao.visepanda.data.repository.TripRepository(getApplication())
        )
        repository.save(legacy.toTripAsset())
    }
}
```

并在 `Done` 分支里调用：

```kotlin
maybeSaveTrip(cityId, assistant.content)
```

把 `RewriteNavHost.kt` 中 `Trips` 路由替换为：

```kotlin
composable(RewriteRoutes.TRIPS) {
    TripsScreen(
        onStartPlanning = {
            navController.navigate(RewriteRoutes.CHAT) {
                launchSingleTop = true
            }
        }
    )
}
```

- [ ] **Step 4: 运行测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.chat.ChatTripSaveTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 Trips 闭环**

```bash
git add app/src/main/java/space/jtcao/visepanda/feature/trips app/src/main/java/space/jtcao/visepanda/feature/chat/ChatViewModel.kt app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt app/src/test/java/space/jtcao/visepanda/feature/chat/ChatTripSaveTest.kt
git commit -m "feat: add rewrite trips flow"
```

---

### Task 6: 完成 Tools 帮助中心并接入入口

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/domain/model/ToolEntry.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/repository/ToolRepository.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetToolEntriesUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/tools/ToolRepositoryImpl.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/tools/ToolsUiState.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/tools/ToolsViewModel.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/tools/ToolsScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`

- [ ] **Step 1: 写失败测试，固定 ToolEntry 数量**

在 `app/src/test/java/space/jtcao/visepanda/data/tools/ToolRepositoryImplTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.tools

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRepositoryImplTest {

    @Test
    fun `repository should expose at least six travel help entries`() = runBlocking {
        val result = ToolRepositoryImpl().getEntries()
        assertTrue(result.size >= 6)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.tools.ToolRepositoryImplTest"
```

Expected: FAIL，相关模型和仓储尚不存在。

- [ ] **Step 3: 实现 Tools domain / data / feature**

创建 `ToolEntry.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class ToolEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String
)
```

创建 `ToolRepository.kt`：

```kotlin
package space.jtcao.visepanda.domain.repository

import space.jtcao.visepanda.domain.model.ToolEntry

interface ToolRepository {
    suspend fun getEntries(): List<ToolEntry>
}
```

创建 `GetToolEntriesUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.ToolEntry
import space.jtcao.visepanda.domain.repository.ToolRepository

class GetToolEntriesUseCase(
    private val repository: ToolRepository
) {
    suspend operator fun invoke(): List<ToolEntry> = repository.getEntries()
}
```

创建 `ToolRepositoryImpl.kt`：

```kotlin
package space.jtcao.visepanda.data.tools

import space.jtcao.visepanda.domain.model.ToolEntry
import space.jtcao.visepanda.domain.repository.ToolRepository

class ToolRepositoryImpl : ToolRepository {
    override suspend fun getEntries(): List<ToolEntry> = listOf(
        ToolEntry("payment", "Payment", "Mobile payment basics for China", "Essential"),
        ToolEntry("visa", "Visa", "Entry documents and practical reminders", "Essential"),
        ToolEntry("sim", "SIM / Internet", "Stay connected on arrival", "Connectivity"),
        ToolEntry("emergency", "Emergency", "What to do in urgent situations", "Safety"),
        ToolEntry("etiquette", "Etiquette", "Small social cues that matter", "Culture"),
        ToolEntry("language", "Useful Chinese", "Helpful words for food, taxis and hotels", "Language")
    )
}
```

创建 `ToolsUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.tools

import space.jtcao.visepanda.domain.model.ToolEntry

sealed interface ToolsUiState {
    data object Loading : ToolsUiState
    data class Success(val entries: List<ToolEntry>) : ToolsUiState
    data class Error(val message: String) : ToolsUiState
}
```

创建 `ToolsViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.tools.ToolRepositoryImpl
import space.jtcao.visepanda.domain.usecase.GetToolEntriesUseCase

class ToolsViewModel(
    private val getToolEntries: GetToolEntriesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = try {
                ToolsUiState.Success(getToolEntries())
            } catch (t: Throwable) {
                ToolsUiState.Error(t.message ?: "Failed to load tools")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ToolsViewModel(GetToolEntriesUseCase(ToolRepositoryImpl()))
            }
        }
    }
}
```

创建 `ToolsScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel = viewModel(factory = ToolsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val uiState = state) {
        ToolsUiState.Loading -> Text("Loading tools...")
        is ToolsUiState.Error -> Text(uiState.message)
        is ToolsUiState.Success -> {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VpSectionHeader(
                        title = "Travel Help Center",
                        subtitle = "Essential tools for a smooth China trip."
                    )
                }
                items(uiState.entries, key = { it.id }) { entry ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleLarge)
                        Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium)
                        Text(entry.category, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
```

把 `RewriteRoutes.kt` 增加：

```kotlin
const val TOOLS = "tools"
```

把 `RewriteNavHost.kt` 增加：

```kotlin
composable(RewriteRoutes.TOOLS) {
    ToolsScreen()
}
```

并在 `HomeScreen.kt` 新增一个按钮：

```kotlin
Button(onClick = onOpenTools) {
    Text("Travel Help Center")
}
```

同时将 `HomeScreen` 签名改成：

```kotlin
fun HomeScreen(
    onOpenExplore: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenTools: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
)
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.tools.ToolRepositoryImplTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 Tools 帮助中心**

```bash
git add app/src/main/java/space/jtcao/visepanda/domain/model/ToolEntry.kt app/src/main/java/space/jtcao/visepanda/domain/repository/ToolRepository.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/GetToolEntriesUseCase.kt app/src/main/java/space/jtcao/visepanda/data/tools/ToolRepositoryImpl.kt app/src/main/java/space/jtcao/visepanda/feature/tools app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt app/src/test/java/space/jtcao/visepanda/data/tools/ToolRepositoryImplTest.kt
git commit -m "feat: add rewrite tools center"
```

---

### Task 7: 更新 handoff 并做阶段 2 验证

**Files:**
- Modify: `HANDOFF.md`

- [ ] **Step 1: 在 handoff 中更新 Rewrite Track**

将 `Rewrite Track` 更新为：

```md
- Main rewrite entry now starts from `VisePandaRewriteApp`
- Phase 1: design system, Home, Explore, City Detail, mock/real destination data
- Phase 2: Chat, Trips, and Tools now live under the rewrite feature tree
- Legacy `ui/*` screens still exist in the repo but are no longer the target architecture
```

- [ ] **Step 2: 补充新的已实现能力**

在 `HANDOFF.md` 的 `Current State` 中新增：

```md
- Rewrite Chat Screen — AI Travel Concierge flow on the new feature architecture
- Rewrite Trips Screen — trip assets loaded from the new rewrite flow
- Rewrite Tools Screen — travel help center entry integrated from Home
```

- [ ] **Step 3: 运行阶段 2 验证**

Run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug --stacktrace
```

Expected:

- 单测通过
- `assembleDebug` 至少能暴露与新 feature 架构相关的真实编译问题，而不是仍停留在旧主线

- [ ] **Step 4: 提交阶段 2 收尾**

```bash
git add HANDOFF.md
git commit -m "docs: mark rewrite phase 2 entry"
```

- [ ] **Step 5: 推送**

```bash
git push origin HEAD
```

Expected: 远程仓库进入包含 Chat / Trips / Tools 的 rewrite 主线。

---

## 自检

### 规格覆盖

- Chat 重写：Task 1-3
- Trips 资产与保存闭环：Task 4-5
- Tools 帮助中心：Task 6
- handoff 与阶段验证：Task 7

没有回头扩张到 Home / Explore / Destination 的无关重构。

### 占位检查

计划中没有 `TODO`、`TBD` 或“类似 Task N”的跳步写法。所有关键步骤都给出了具体代码、命令和提交方式。

### 类型一致性

- `ChatSseEvent`、`ChatRepository`、`StreamChatUseCase` 前后命名一致
- `TripAsset` 与 `TripAssetRepository` 前后命名一致
- `Tools` 相关模型均统一使用 `ToolEntry`
