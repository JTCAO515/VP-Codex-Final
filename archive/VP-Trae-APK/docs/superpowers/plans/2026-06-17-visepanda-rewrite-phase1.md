# VisePanda Rewrite Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立全新的 Android 原生重写主线工程，完成设计系统、顶层导航、Home、Explore、City Detail 和可切换的目的地 mock/real 数据链路。

**Architecture:** 这一阶段只做新主线的基础骨架，不继续修旧页面。实现方式是先建立新的包结构与设计系统，再接入 `Home / Explore / City Detail` 的页面和目的地数据域；数据层默认支持 mock/real 双通道，页面层只消费统一的 `UiState`。本阶段故意不接入 Chat 和 Trips 的复杂主链路，以避免首轮重写失控，并为下一阶段单独计划留出清晰边界。

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, ViewModel, StateFlow, Kotlin Serialization, DataStore, JUnit4

---

## 文件结构

### 需要新增

- `app/src/main/java/space/jtcao/visepanda/app/VisePandaRewriteApp.kt`
  新的应用级 Compose 入口，承载重写后的导航与主题。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/VpColors.kt`
  东方奢雅配色 token。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/VpTypography.kt`
  标题、正文和品牌排版 token。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/VpTheme.kt`
  统一主题入口。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpScaffold.kt`
  新的页面容器和底部导航外壳。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpHeroCard.kt`
  首页 Hero 组件。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpSectionHeader.kt`
  章节标题组件。
- `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpDestinationCard.kt`
  城市卡片组件。
- `app/src/main/java/space/jtcao/visepanda/core/common/AppMode.kt`
  `mock` / `real` 模式开关定义。
- `app/src/main/java/space/jtcao/visepanda/core/common/AppConfig.kt`
  当前 app 运行模式与配置。
- `app/src/main/java/space/jtcao/visepanda/domain/model/DestinationSummary.kt`
  城市摘要 domain model。
- `app/src/main/java/space/jtcao/visepanda/domain/model/DestinationDetail.kt`
  城市详情 domain model。
- `app/src/main/java/space/jtcao/visepanda/domain/repository/DestinationRepository.kt`
  目的地仓储接口。
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetFeaturedDestinationsUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetExploreDestinationsUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetDestinationDetailUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/data/destination/mock/MockDestinationDataSource.kt`
- `app/src/main/java/space/jtcao/visepanda/data/destination/remote/RemoteDestinationDataSource.kt`
- `app/src/main/java/space/jtcao/visepanda/data/destination/DestinationRepositoryImpl.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/home/HomeUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/home/HomeViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/destination/CityDetailUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/destination/CityDetailViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/destination/CityDetailScreen.kt`
- `app/src/test/java/space/jtcao/visepanda/data/destination/DestinationRepositoryImplTest.kt`
- `app/src/test/java/space/jtcao/visepanda/domain/usecase/GetFeaturedDestinationsUseCaseTest.kt`
- `app/src/test/java/space/jtcao/visepanda/feature/home/HomeViewModelTest.kt`

### 需要修改

- `app/src/main/java/space/jtcao/visepanda/MainActivity.kt`
  切换到新的 app shell。
- `app/src/main/AndroidManifest.xml`
  保持应用入口一致，必要时更新 `application` 配置。
- `app/build.gradle.kts`
  补充测试依赖和序列化设置。

---

### Task 1: 建立重写阶段的工程入口与测试依赖

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/space/jtcao/visepanda/MainActivity.kt`
- Create: `app/src/test/java/space/jtcao/visepanda/domain/usecase/GetFeaturedDestinationsUseCaseTest.kt`

- [ ] **Step 1: 写第一条失败测试，固定新 domain 用例入口**

在 `app/src/test/java/space/jtcao/visepanda/domain/usecase/GetFeaturedDestinationsUseCaseTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetFeaturedDestinationsUseCaseTest {

    @Test
    fun `invoke should return top featured destinations from repository`() {
        val repo = object : DestinationRepository {
            override suspend fun getFeaturedDestinations(): List<DestinationSummary> {
                return listOf(
                    DestinationSummary("shanghai", "Shanghai", "Magic City", "Editorial luxury", 31.2304, 121.4737),
                    DestinationSummary("beijing", "Beijing", "Ancient Capital", "Imperial culture", 39.9042, 116.4074)
                )
            }

            override suspend fun getExploreDestinations(): List<DestinationSummary> = emptyList()

            override suspend fun getDestinationDetail(cityId: String) =
                throw IllegalStateException("not needed")
        }

        val useCase = GetFeaturedDestinationsUseCase(repo)

        val result = kotlinx.coroutines.runBlocking { useCase() }

        assertEquals(2, result.size)
        assertEquals("shanghai", result.first().id)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.domain.usecase.GetFeaturedDestinationsUseCaseTest"
```

Expected: FAIL，提示 `DestinationRepository`、`DestinationSummary`、`GetFeaturedDestinationsUseCase` 尚不存在。

- [ ] **Step 3: 为测试补最小依赖并切主入口到新壳子**

在 `app/build.gradle.kts` 的 `dependencies` 中保证存在：

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
```

把 `app/src/main/java/space/jtcao/visepanda/MainActivity.kt` 改成：

```kotlin
package space.jtcao.visepanda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import space.jtcao.visepanda.app.VisePandaRewriteApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VisePandaRewriteApp()
        }
    }
}
```

- [ ] **Step 4: 新增最小 domain 占位实现让测试进入可编译状态**

创建 `DestinationSummary.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class DestinationSummary(
    val id: String,
    val name: String,
    val tagline: String,
    val vibe: String,
    val lat: Double,
    val lng: Double
)
```

创建 `DestinationDetail.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class DestinationDetail(
    val id: String,
    val name: String,
    val headline: String,
    val bestDays: String,
    val budget: String,
    val highlights: List<String>,
    val foods: List<String>,
    val tips: List<String>
)
```

创建 `DestinationRepository.kt`：

```kotlin
package space.jtcao.visepanda.domain.repository

import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

interface DestinationRepository {
    suspend fun getFeaturedDestinations(): List<DestinationSummary>
    suspend fun getExploreDestinations(): List<DestinationSummary>
    suspend fun getDestinationDetail(cityId: String): DestinationDetail
}
```

创建 `GetFeaturedDestinationsUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetFeaturedDestinationsUseCase(
    private val repository: DestinationRepository
) {
    suspend operator fun invoke(): List<DestinationSummary> {
        return repository.getFeaturedDestinations()
    }
}
```

- [ ] **Step 5: 重跑测试确认通过并提交**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.domain.usecase.GetFeaturedDestinationsUseCaseTest"
```

Expected: PASS。

```bash
git add app/build.gradle.kts app/src/main/java/space/jtcao/visepanda/MainActivity.kt app/src/main/java/space/jtcao/visepanda/domain/model/DestinationSummary.kt app/src/main/java/space/jtcao/visepanda/domain/model/DestinationDetail.kt app/src/main/java/space/jtcao/visepanda/domain/repository/DestinationRepository.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/GetFeaturedDestinationsUseCase.kt app/src/test/java/space/jtcao/visepanda/domain/usecase/GetFeaturedDestinationsUseCaseTest.kt
git commit -m "feat: bootstrap rewrite domain entry"
```

---

### Task 2: 建立东方奢雅设计系统与新的 App Shell

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/app/VisePandaRewriteApp.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/VpColors.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/VpTypography.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/VpTheme.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpScaffold.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpHeroCard.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpSectionHeader.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/designsystem/components/VpDestinationCard.kt`

- [ ] **Step 1: 先写一个设计 token 的失败测试**

在 `app/src/test/java/space/jtcao/visepanda/core/designsystem/VpThemeSmokeTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.core.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class VpThemeSmokeTest {

    @Test
    fun `gold accent should stay stable`() {
        assertEquals(0xFFC9A45C, VpGold.value)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.core.designsystem.VpThemeSmokeTest"
```

Expected: FAIL，`VpGold` 尚不存在。

- [ ] **Step 3: 实现最小设计 token 与主题壳**

创建 `VpColors.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem

import androidx.compose.ui.graphics.Color

val VpInk = Color(0xFF101114)
val VpGold = Color(0xFFC9A45C)
val VpJade = Color(0xFF4E6E63)
val VpIvory = Color(0xFFF4ECDD)
val VpMist = Color(0xFF1A1C20)
```

创建 `VpTypography.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem

import androidx.compose.material3.Typography

val VpTypography = Typography()
```

创建 `VpTheme.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VpScheme = darkColorScheme(
    primary = VpGold,
    secondary = VpJade,
    background = VpInk,
    surface = VpMist,
    onPrimary = VpInk,
    onBackground = VpIvory,
    onSurface = VpIvory
)

@Composable
fun VpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VpScheme,
        typography = VpTypography,
        content = content
    )
}
```

创建 `VpSectionHeader.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun VpSectionHeader(title: String, subtitle: String? = null) {
    Text(text = title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
    if (subtitle != null) {
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f))
    }
}
```

创建 `VpHeroCard.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VpHeroCard(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Text(text = "VISEPANDA", color = MaterialTheme.colorScheme.primary)
        Text(text = title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f))
    }
}
```

创建 `VpDestinationCard.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VpDestinationCard(
    title: String,
    tagline: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(tagline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
```

创建 `VpScaffold.kt`：

```kotlin
package space.jtcao.visepanda.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VpScaffold(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        label = { Text(tab) },
                        icon = {}
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content(padding)
        }
    }
}
```

创建 `VisePandaRewriteApp.kt`：

```kotlin
package space.jtcao.visepanda.app

import androidx.compose.runtime.Composable
import space.jtcao.visepanda.core.designsystem.VpTheme
import space.jtcao.visepanda.feature.navigation.RewriteNavHost

@Composable
fun VisePandaRewriteApp() {
    VpTheme {
        RewriteNavHost()
    }
}
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.core.designsystem.VpThemeSmokeTest"
```

Expected: PASS。

- [ ] **Step 5: 提交设计系统骨架**

```bash
git add app/src/main/java/space/jtcao/visepanda/app/VisePandaRewriteApp.kt app/src/main/java/space/jtcao/visepanda/core/designsystem app/src/test/java/space/jtcao/visepanda/core/designsystem/VpThemeSmokeTest.kt
git commit -m "feat: add rewrite design system shell"
```

---

### Task 3: 建立顶层导航与新的 `Home / Explore / Trips` 外壳

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreScreen.kt`

- [ ] **Step 1: 写导航路由失败测试**

在 `app/src/test/java/space/jtcao/visepanda/feature/navigation/RewriteRoutesTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.feature.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RewriteRoutesTest {

    @Test
    fun `city detail route should encode city id`() {
        assertEquals("destination/shanghai", RewriteRoutes.destination("shanghai"))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.navigation.RewriteRoutesTest"
```

Expected: FAIL，`RewriteRoutes` 不存在。

- [ ] **Step 3: 实现最小导航壳**

创建 `RewriteRoutes.kt`：

```kotlin
package space.jtcao.visepanda.feature.navigation

object RewriteRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val CHAT = "chat"
    const val TRIPS = "trips"
    const val DESTINATION = "destination/{cityId}"

    fun destination(cityId: String): String = "destination/$cityId"
}
```

创建 `HomeScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.jtcao.visepanda.core.designsystem.components.VpHeroCard
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun HomeScreen(
    onOpenExplore: () -> Unit,
    onOpenChat: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        VpHeroCard(
            title = "Travel China Beautifully",
            subtitle = "An elegant AI travel companion for modern explorers."
        )
        VpSectionHeader(
            title = "Curated China",
            subtitle = "Featured cities and editorial routes"
        )
    }
}
```

创建 `ExploreScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.explore

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExploreScreen() {
    Text("Explore")
}
```

创建 `RewriteNavHost.kt`：

```kotlin
package space.jtcao.visepanda.feature.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import space.jtcao.visepanda.core.designsystem.components.VpScaffold
import space.jtcao.visepanda.feature.explore.ExploreScreen
import space.jtcao.visepanda.feature.home.HomeScreen

@Composable
fun RewriteNavHost() {
    val navController = rememberNavController()
    val tabs = listOf("Home", "Explore", "Chat", "Trips")
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: RewriteRoutes.HOME

    VpScaffold(
        tabs = tabs,
        selectedTab = when (currentRoute) {
            RewriteRoutes.EXPLORE -> "Explore"
            RewriteRoutes.CHAT -> "Chat"
            RewriteRoutes.TRIPS -> "Trips"
            else -> "Home"
        },
        onTabSelected = { tab ->
            val route = when (tab) {
                "Explore" -> RewriteRoutes.EXPLORE
                "Chat" -> RewriteRoutes.CHAT
                "Trips" -> RewriteRoutes.TRIPS
                else -> RewriteRoutes.HOME
            }
            navController.navigate(route)
        }
    ) {
        NavHost(navController = navController, startDestination = RewriteRoutes.HOME) {
            composable(RewriteRoutes.HOME) {
                HomeScreen(
                    onOpenExplore = { navController.navigate(RewriteRoutes.EXPLORE) },
                    onOpenChat = { navController.navigate(RewriteRoutes.CHAT) }
                )
            }
            composable(RewriteRoutes.EXPLORE) { ExploreScreen() }
            composable(RewriteRoutes.CHAT) { androidx.compose.material3.Text("Chat") }
            composable(RewriteRoutes.TRIPS) { androidx.compose.material3.Text("Trips") }
        }
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.navigation.RewriteRoutesTest"
```

Expected: PASS。

- [ ] **Step 5: 提交导航外壳**

```bash
git add app/src/main/java/space/jtcao/visepanda/feature/navigation app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreScreen.kt app/src/test/java/space/jtcao/visepanda/feature/navigation/RewriteRoutesTest.kt
git commit -m "feat: add rewrite navigation shell"
```

---

### Task 4: 建立目的地 mock / real 数据链路

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/core/common/AppMode.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/core/common/AppConfig.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/destination/mock/MockDestinationDataSource.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/destination/remote/RemoteDestinationDataSource.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/destination/DestinationRepositoryImpl.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/data/destination/DestinationRepositoryImplTest.kt`

- [ ] **Step 1: 写失败测试，固定 mock / real 路由逻辑**

在 `app/src/test/java/space/jtcao/visepanda/data/destination/DestinationRepositoryImplTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.data.destination

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.core.common.AppMode
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

class DestinationRepositoryImplTest {

    @Test
    fun `mock mode should read from mock data source`() = runBlocking {
        val repo = DestinationRepositoryImpl(
            appMode = AppMode.MOCK,
            remote = object : DestinationDataSource {
                override suspend fun getFeatured() = error("remote should not be used")
                override suspend fun getExplore() = error("remote should not be used")
                override suspend fun getDetail(cityId: String) = error("remote should not be used")
            },
            mock = object : DestinationDataSource {
                override suspend fun getFeatured() = listOf(
                    DestinationSummary("hangzhou", "Hangzhou", "West Lake", "Poetic water city", 30.2741, 120.1551)
                )
                override suspend fun getExplore() = emptyList<DestinationSummary>()
                override suspend fun getDetail(cityId: String) =
                    DestinationDetail(cityId, "Hangzhou", "Poetic city", "3 days", "$$$", emptyList(), emptyList(), emptyList())
            }
        )

        val result = repo.getFeaturedDestinations()

        assertEquals("hangzhou", result.first().id)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.destination.DestinationRepositoryImplTest"
```

Expected: FAIL，`AppMode`、`DestinationRepositoryImpl`、`DestinationDataSource` 不存在。

- [ ] **Step 3: 实现最小 mock / real 数据层**

创建 `AppMode.kt`：

```kotlin
package space.jtcao.visepanda.core.common

enum class AppMode {
    MOCK,
    REAL
}
```

创建 `AppConfig.kt`：

```kotlin
package space.jtcao.visepanda.core.common

object AppConfig {
    val appMode: AppMode = AppMode.MOCK
}
```

创建 `DestinationDataSource.kt`：

```kotlin
package space.jtcao.visepanda.data.destination

import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

interface DestinationDataSource {
    suspend fun getFeatured(): List<DestinationSummary>
    suspend fun getExplore(): List<DestinationSummary>
    suspend fun getDetail(cityId: String): DestinationDetail
}
```

创建 `MockDestinationDataSource.kt`：

```kotlin
package space.jtcao.visepanda.data.destination.mock

import space.jtcao.visepanda.data.destination.DestinationDataSource
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

class MockDestinationDataSource : DestinationDataSource {
    override suspend fun getFeatured(): List<DestinationSummary> = listOf(
        DestinationSummary("shanghai", "Shanghai", "Magic City", "Editorial luxury", 31.2304, 121.4737),
        DestinationSummary("beijing", "Beijing", "Ancient Capital", "Imperial culture", 39.9042, 116.4074),
        DestinationSummary("hangzhou", "Hangzhou", "West Lake", "Poetic water city", 30.2741, 120.1551)
    )

    override suspend fun getExplore(): List<DestinationSummary> = getFeatured() + listOf(
        DestinationSummary("chengdu", "Chengdu", "Panda capital", "Tea house ease", 30.5728, 104.0668)
    )

    override suspend fun getDetail(cityId: String): DestinationDetail =
        DestinationDetail(
            id = cityId,
            name = cityId.replaceFirstChar { it.uppercase() },
            headline = "An elegant destination for first-time China travel.",
            bestDays = "3-4 days",
            budget = "$$$",
            highlights = listOf("Iconic skyline", "Historic quarters", "Night strolls"),
            foods = listOf("Signature noodles", "Local brunch", "Tea desserts"),
            tips = listOf("Carry Alipay", "Wear comfortable shoes", "Start early for popular sites")
        )
}
```

创建 `RemoteDestinationDataSource.kt`：

```kotlin
package space.jtcao.visepanda.data.destination.remote

import space.jtcao.visepanda.data.destination.DestinationDataSource
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

class RemoteDestinationDataSource : DestinationDataSource {
    override suspend fun getFeatured(): List<DestinationSummary> = emptyList()
    override suspend fun getExplore(): List<DestinationSummary> = emptyList()
    override suspend fun getDetail(cityId: String): DestinationDetail {
        error("Remote data source will be implemented in phase 2 task extension")
    }
}
```

创建 `DestinationRepositoryImpl.kt`：

```kotlin
package space.jtcao.visepanda.data.destination

import space.jtcao.visepanda.core.common.AppMode
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class DestinationRepositoryImpl(
    private val appMode: AppMode,
    private val remote: DestinationDataSource,
    private val mock: DestinationDataSource
) : DestinationRepository {

    private val active: DestinationDataSource
        get() = if (appMode == AppMode.MOCK) mock else remote

    override suspend fun getFeaturedDestinations(): List<DestinationSummary> = active.getFeatured()

    override suspend fun getExploreDestinations(): List<DestinationSummary> = active.getExplore()

    override suspend fun getDestinationDetail(cityId: String): DestinationDetail = active.getDetail(cityId)
}
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.destination.DestinationRepositoryImplTest"
```

Expected: PASS。

- [ ] **Step 5: 提交目的地数据层**

```bash
git add app/src/main/java/space/jtcao/visepanda/core/common app/src/main/java/space/jtcao/visepanda/data/destination app/src/test/java/space/jtcao/visepanda/data/destination/DestinationRepositoryImplTest.kt
git commit -m "feat: add rewrite destination data sources"
```

---

### Task 5: 完成 `Home` 页面和数据联动

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/feature/home/HomeUiState.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/home/HomeViewModel.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/feature/home/HomeViewModelTest.kt`

- [ ] **Step 1: 写失败测试，固定 Home 页面状态机**

在 `app/src/test/java/space/jtcao/visepanda/feature/home/HomeViewModelTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.feature.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository
import space.jtcao.visepanda.domain.usecase.GetFeaturedDestinationsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `load should expose success state with featured destinations`() = runTest {
        val repo = object : DestinationRepository {
            override suspend fun getFeaturedDestinations() = listOf(
                DestinationSummary("beijing", "Beijing", "Ancient Capital", "Imperial culture", 39.9042, 116.4074)
            )

            override suspend fun getExploreDestinations(): List<DestinationSummary> = emptyList()
            override suspend fun getDestinationDetail(cityId: String) =
                DestinationDetail(cityId, "Beijing", "Ancient Capital", "4 days", "$$$", emptyList(), emptyList(), emptyList())
        }

        val viewModel = HomeViewModel(GetFeaturedDestinationsUseCase(repo))

        viewModel.load()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.home.HomeViewModelTest"
```

Expected: FAIL，`HomeViewModel` / `HomeUiState` 尚不存在或接口不匹配。

- [ ] **Step 3: 实现最小 Home 状态与渲染**

创建 `HomeUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.home

import space.jtcao.visepanda.domain.model.DestinationSummary

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val featured: List<DestinationSummary>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

创建 `HomeViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.domain.usecase.GetFeaturedDestinationsUseCase

class HomeViewModel(
    private val getFeaturedDestinations: GetFeaturedDestinationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = try {
                HomeUiState.Success(getFeaturedDestinations())
            } catch (t: Throwable) {
                HomeUiState.Error(t.message ?: "Failed to load home")
            }
        }
    }
}
```

把 `HomeScreen.kt` 改成：

```kotlin
package space.jtcao.visepanda.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpDestinationCard
import space.jtcao.visepanda.core.designsystem.components.VpHeroCard
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun HomeScreen(
    onOpenExplore: () -> Unit,
    onOpenChat: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val uiState = state) {
        is HomeUiState.Loading -> Text("Loading...")
        is HomeUiState.Error -> Text(uiState.message)
        is HomeUiState.Success -> {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    VpHeroCard(
                        title = "Travel China Beautifully",
                        subtitle = "An elegant AI travel companion for modern explorers."
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onOpenChat) { Text("Plan with AI") }
                    Spacer(Modifier.height(24.dp))
                    VpSectionHeader(
                        title = "Featured Destinations",
                        subtitle = "Curated cities for the first release"
                    )
                    Spacer(Modifier.height(12.dp))
                }
                items(uiState.featured) { item ->
                    VpDestinationCard(
                        title = item.name,
                        tagline = item.tagline,
                        onClick = onOpenExplore
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
```

- [ ] **Step 4: 重跑测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.home.HomeViewModelTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 Home 主流程**

```bash
git add app/src/main/java/space/jtcao/visepanda/feature/home app/src/test/java/space/jtcao/visepanda/feature/home/HomeViewModelTest.kt
git commit -m "feat: add rewrite home flow"
```

---

### Task 6: 完成 Explore 与 City Detail 主路径

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetExploreDestinationsUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/GetDestinationDetailUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreUiState.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreViewModel.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/explore/ExploreScreen.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/destination/CityDetailUiState.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/destination/CityDetailViewModel.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/destination/CityDetailScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`

- [ ] **Step 1: 写 Explore 用例失败测试**

在 `app/src/test/java/space/jtcao/visepanda/domain/usecase/GetExploreDestinationsUseCaseTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.domain.usecase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetExploreDestinationsUseCaseTest {

    @Test
    fun `invoke should return explore destinations`() = runBlocking {
        val repo = object : DestinationRepository {
            override suspend fun getFeaturedDestinations(): List<DestinationSummary> = emptyList()
            override suspend fun getExploreDestinations(): List<DestinationSummary> = listOf(
                DestinationSummary("chengdu", "Chengdu", "Panda capital", "Tea house ease", 30.5728, 104.0668)
            )
            override suspend fun getDestinationDetail(cityId: String) =
                DestinationDetail(cityId, "Chengdu", "Panda capital", "3 days", "$$", emptyList(), emptyList(), emptyList())
        }

        val result = GetExploreDestinationsUseCase(repo).invoke()

        assertEquals("chengdu", result.first().id)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.domain.usecase.GetExploreDestinationsUseCaseTest"
```

Expected: FAIL，相关 use case 尚不存在。

- [ ] **Step 3: 实现 Explore / Detail 用例和 UI**

创建 `GetExploreDestinationsUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetExploreDestinationsUseCase(
    private val repository: DestinationRepository
) {
    suspend operator fun invoke(): List<DestinationSummary> {
        return repository.getExploreDestinations()
    }
}
```

创建 `GetDestinationDetailUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetDestinationDetailUseCase(
    private val repository: DestinationRepository
) {
    suspend operator fun invoke(cityId: String): DestinationDetail {
        return repository.getDestinationDetail(cityId)
    }
}
```

创建 `ExploreUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.explore

import space.jtcao.visepanda.domain.model.DestinationSummary

sealed interface ExploreUiState {
    data object Loading : ExploreUiState
    data class Success(val destinations: List<DestinationSummary>) : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}
```

创建 `ExploreViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.domain.usecase.GetExploreDestinationsUseCase

class ExploreViewModel(
    private val getExploreDestinations: GetExploreDestinationsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = try {
                ExploreUiState.Success(getExploreDestinations())
            } catch (t: Throwable) {
                ExploreUiState.Error(t.message ?: "Failed to load explore")
            }
        }
    }
}
```

把 `ExploreScreen.kt` 改成：

```kotlin
package space.jtcao.visepanda.feature.explore

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpDestinationCard
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun ExploreScreen(
    onOpenDestination: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val uiState = state) {
        is ExploreUiState.Loading -> androidx.compose.material3.Text("Loading...")
        is ExploreUiState.Error -> androidx.compose.material3.Text(uiState.message)
        is ExploreUiState.Success -> {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    VpSectionHeader(
                        title = "Explore China",
                        subtitle = "Browse cities and move into trip planning."
                    )
                    Spacer(Modifier.height(16.dp))
                }
                items(uiState.destinations) { item ->
                    VpDestinationCard(
                        title = item.name,
                        tagline = item.vibe,
                        onClick = { onOpenDestination(item.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
```

创建 `CityDetailUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.destination

import space.jtcao.visepanda.domain.model.DestinationDetail

sealed interface CityDetailUiState {
    data object Loading : CityDetailUiState
    data class Success(val detail: DestinationDetail) : CityDetailUiState
    data class Error(val message: String) : CityDetailUiState
}
```

创建 `CityDetailViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.destination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.domain.usecase.GetDestinationDetailUseCase

class CityDetailViewModel(
    private val getDestinationDetail: GetDestinationDetailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<CityDetailUiState>(CityDetailUiState.Loading)
    val uiState: StateFlow<CityDetailUiState> = _uiState.asStateFlow()

    fun load(cityId: String) {
        viewModelScope.launch {
            _uiState.value = try {
                CityDetailUiState.Success(getDestinationDetail(cityId))
            } catch (t: Throwable) {
                CityDetailUiState.Error(t.message ?: "Failed to load destination")
            }
        }
    }
}
```

创建 `CityDetailScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.destination

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpHeroCard

@Composable
fun CityDetailScreen(
    cityId: String,
    onPlanTrip: (String) -> Unit,
    viewModel: CityDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(cityId) { viewModel.load(cityId) }

    when (val uiState = state) {
        is CityDetailUiState.Loading -> Text("Loading...")
        is CityDetailUiState.Error -> Text(uiState.message)
        is CityDetailUiState.Success -> {
            val detail = uiState.detail
            Column(modifier = Modifier.padding(20.dp)) {
                VpHeroCard(title = detail.name, subtitle = detail.headline)
                Spacer(Modifier.height(20.dp))
                Text("Best stay: ${detail.bestDays}")
                Text("Budget: ${detail.budget}")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { onPlanTrip(detail.id) }) {
                    Text("Plan my trip")
                }
            }
        }
    }
}
```

把 `RewriteNavHost.kt` 更新成：

```kotlin
package space.jtcao.visepanda.feature.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import space.jtcao.visepanda.core.designsystem.components.VpScaffold
import space.jtcao.visepanda.feature.destination.CityDetailScreen
import space.jtcao.visepanda.feature.explore.ExploreScreen
import space.jtcao.visepanda.feature.home.HomeScreen

@Composable
fun RewriteNavHost() {
    val navController = rememberNavController()
    val tabs = listOf("Home", "Explore", "Chat", "Trips")
    val backStack = navController.currentBackStackEntryAsState().value
    val currentRoute = backStack?.destination?.route ?: RewriteRoutes.HOME

    VpScaffold(
        tabs = tabs,
        selectedTab = when {
            currentRoute.startsWith("destination/") -> "Explore"
            currentRoute == RewriteRoutes.EXPLORE -> "Explore"
            currentRoute == RewriteRoutes.CHAT -> "Chat"
            currentRoute == RewriteRoutes.TRIPS -> "Trips"
            else -> "Home"
        },
        onTabSelected = { tab ->
            val route = when (tab) {
                "Explore" -> RewriteRoutes.EXPLORE
                "Chat" -> RewriteRoutes.CHAT
                "Trips" -> RewriteRoutes.TRIPS
                else -> RewriteRoutes.HOME
            }
            navController.navigate(route)
        }
    ) {
        NavHost(navController = navController, startDestination = RewriteRoutes.HOME) {
            composable(RewriteRoutes.HOME) {
                HomeScreen(
                    onOpenExplore = { navController.navigate(RewriteRoutes.EXPLORE) },
                    onOpenChat = { navController.navigate(RewriteRoutes.CHAT) }
                )
            }
            composable(RewriteRoutes.EXPLORE) {
                ExploreScreen(
                    onOpenDestination = { cityId ->
                        navController.navigate(RewriteRoutes.destination(cityId))
                    }
                )
            }
            composable(RewriteRoutes.CHAT) { androidx.compose.material3.Text("Chat") }
            composable(RewriteRoutes.TRIPS) { androidx.compose.material3.Text("Trips") }
            composable(
                route = RewriteRoutes.DESTINATION,
                arguments = listOf(navArgument("cityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cityId = backStackEntry.arguments?.getString("cityId").orEmpty()
                CityDetailScreen(
                    cityId = cityId,
                    onPlanTrip = { navController.navigate(RewriteRoutes.CHAT) }
                )
            }
        }
    }
}
```

- [ ] **Step 4: 运行用例测试确认通过**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.domain.usecase.GetExploreDestinationsUseCaseTest"
```

Expected: PASS。

- [ ] **Step 5: 提交 Explore / Detail 主路径**

```bash
git add app/src/main/java/space/jtcao/visepanda/domain/usecase/GetExploreDestinationsUseCase.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/GetDestinationDetailUseCase.kt app/src/main/java/space/jtcao/visepanda/feature/explore app/src/main/java/space/jtcao/visepanda/feature/destination app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt app/src/test/java/space/jtcao/visepanda/domain/usecase/GetExploreDestinationsUseCaseTest.kt
git commit -m "feat: add rewrite explore and destination flow"
```

---

### Task 7: 进行第一阶段收尾验证并冻结旧入口

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `HANDOFF.md`

- [ ] **Step 1: 在 handoff 中写明旧主线冻结、新主线进入 phase 1**

把 `HANDOFF.md` 的顶部状态更新为：

```md
> **Status:** 🧭 Rewrite Phase 1 in progress — new native architecture, design system, and destination flow under active build
```

并新增一节：

```md
## Rewrite Track

- Main rewrite entry now starts from `VisePandaRewriteApp`
- Phase 1 covers: design system, Home, Explore, City Detail, mock/real destination data
- Legacy chat / trips screens remain in repository but are no longer the target architecture
```
```

- [ ] **Step 2: 确认 Manifest 仍指向当前 `MainActivity`**

检查 `app/src/main/AndroidManifest.xml`，保持：

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.VisePanda">
```

如果不一致，修正到以上形态。

- [ ] **Step 3: 运行阶段性验证**

Run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug --stacktrace
```

Expected:

- 单测 PASS
- `assembleDebug` PASS，或只暴露与新重写主线相关的真实编译问题

- [ ] **Step 4: 提交阶段 1 冻结点**

```bash
git add HANDOFF.md app/src/main/AndroidManifest.xml
git commit -m "docs: mark rewrite phase 1 entry"
```

- [ ] **Step 5: 推送并准备下一计划**

```bash
git push origin HEAD
```

Expected: 远程仓库出现重写阶段的基础骨架；下一份计划可以专门针对 `Chat + Trips + Tools`。

---

## 自检

### 规格覆盖

- Android 原生 Compose 重写主线：Task 1-3
- 东方奢雅设计系统：Task 2
- `Home / Explore / Chat / Trips` 顶层结构：Task 3
- Explore 整合城市与地图语义：Task 4-6
- 城市详情页与 CTA：Task 6
- mock / real 双通道：Task 4
- 稳定性与新主线入口冻结：Task 7

本计划刻意不覆盖 `Chat + Trips + Tools` 的完整实现，因为它们会构成下一份独立计划，避免第一阶段范围失控。

### 占位检查

没有 `TODO`、`TBD` 或“后续再说”的实现型占位。涉及远程真实实现的地方已经明确限定为 phase 2 扩展，不属于本阶段提交目标。

### 类型一致性

- `DestinationRepository` 的三个方法前后命名一致
- `RewriteRoutes.destination(cityId)` 在测试和导航实现中一致
- `HomeUiState` / `ExploreUiState` / `CityDetailUiState` 都遵循统一状态表达
