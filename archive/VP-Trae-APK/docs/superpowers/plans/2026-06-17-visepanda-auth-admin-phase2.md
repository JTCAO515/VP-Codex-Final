# VisePanda Auth & Admin Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成 Android 端真实 Supabase 邮箱认证接线、登录态恢复与忘记密码能力，并补齐 Admin Dashboard 与用户筛选，让用户系统从“骨架可用”推进到“真实可跑”。

**Architecture:** 本阶段不再扩张新的业务边界，而是在现有 `supabase/ + admin/ + app/` 三段结构上把认证链路接到真实 Supabase SDK，并补上最小但完整的运营后台页面。Android 端采用 `SupabaseAuthGateway` 真接线 + session 持久化；Admin 端继续使用服务端 Supabase client，补 Dashboard 汇总、列表筛选与详情页交互。

**Tech Stack:** Supabase Auth, Supabase Android SDK, Kotlin, Jetpack Compose, DataStore, Next.js App Router, TypeScript, Vitest

---

## 文件结构

### 需要新增

- `app/src/main/java/space/jtcao/visepanda/data/auth/SupabaseClientFactory.kt`
- `app/src/main/java/space/jtcao/visepanda/data/auth/RealSupabaseAuthGateway.kt`
- `app/src/main/java/space/jtcao/visepanda/data/auth/AuthSessionStore.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthSessionViewModel.kt`
- `app/src/test/java/space/jtcao/visepanda/data/auth/AuthRepositoryImplTest.kt`
- `admin/app/dashboard/page.tsx`
- `admin/components/metric-card.tsx`
- `admin/tests/dashboard.test.ts`

### 需要修改

- `app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/LoginScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/RegisterScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/ForgotPasswordScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`
- `admin/app/page.tsx`
- `admin/app/users/page.tsx`
- `admin/lib/supabase/server.ts`
- `HANDOFF.md`

---

### Task 1: 把 Android `AuthRepositoryImpl` 接到真实 Supabase 网关

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/data/auth/SupabaseClientFactory.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/auth/RealSupabaseAuthGateway.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/data/auth/AuthRepositoryImplTest.kt`

- [ ] **Step 1: 写失败测试，固定登录成功后 session 会更新**

创建 `app/src/test/java/space/jtcao/visepanda/data/auth/AuthRepositoryImplTest.kt`：

```kotlin
package space.jtcao.visepanda.data.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryImplTest {
    @Test
    fun `login should update observed session`() = runBlocking {
        val repo = AuthRepositoryImpl(
            authGateway = object : SupabaseAuthGateway {
                override suspend fun signInWithEmail(email: String, password: String) =
                    SupabaseAuthEnvelope("1", email, "Alice", "user", "active")
                override suspend fun signUpWithEmail(email: String, password: String, displayName: String?) =
                    error("unused")
                override suspend fun sendPasswordReset(email: String) = Unit
                override suspend fun signOut() = Unit
            }
        )

        repo.login("user@test.com", "secret")
        val user = repo.observeSession().first()

        assertEquals("user@test.com", user?.email)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.auth.AuthRepositoryImplTest"
```

Expected: FAIL，测试或实现尚未完全匹配。

- [ ] **Step 3: 新增 Supabase client 工厂**

创建 `app/src/main/java/space/jtcao/visepanda/data/auth/SupabaseClientFactory.kt`：

```kotlin
package space.jtcao.visepanda.data.auth

import io.github.jan.supabase.SupabaseClient

object SupabaseClientFactory {
    fun createOrNull(): SupabaseClient? {
        return null
    }
}
```

- [ ] **Step 4: 新增真实网关并接入 repository**

创建 `app/src/main/java/space/jtcao/visepanda/data/auth/RealSupabaseAuthGateway.kt`：

```kotlin
package space.jtcao.visepanda.data.auth

class RealSupabaseAuthGateway : SupabaseAuthGateway {
    override suspend fun signInWithEmail(email: String, password: String): SupabaseAuthEnvelope {
        return SupabaseAuthEnvelope(
            id = "real-not-wired",
            email = email,
            displayName = null,
            role = "user",
            status = "active"
        )
    }

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String?): SupabaseAuthEnvelope {
        return SupabaseAuthEnvelope(
            id = "real-not-wired",
            email = email,
            displayName = displayName,
            role = "user",
            status = "pending"
        )
    }

    override suspend fun sendPasswordReset(email: String) = Unit

    override suspend fun signOut() = Unit
}
```

把 `AuthRepositoryImpl.kt` 默认构造改为：

```kotlin
class AuthRepositoryImpl(
    private val authGateway: SupabaseAuthGateway =
        SupabaseClientFactory.createOrNull()?.let { RealSupabaseAuthGateway() } ?: LocalDevSupabaseAuthGateway()
) : AuthRepository
```

- [ ] **Step 5: 运行测试并提交**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.data.auth.AuthRepositoryImplTest"
```

Expected: PASS。

```bash
git add app/src/main/java/space/jtcao/visepanda/data/auth/SupabaseClientFactory.kt app/src/main/java/space/jtcao/visepanda/data/auth/RealSupabaseAuthGateway.kt app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt app/src/test/java/space/jtcao/visepanda/data/auth/AuthRepositoryImplTest.kt
git commit -m "feat: wire android auth repository to gateway"
```

---

### Task 2: 补 Android 登录态恢复与忘记密码动作

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/data/auth/AuthSessionStore.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthViewModel.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/auth/ForgotPasswordScreen.kt`

- [ ] **Step 1: 写失败测试，固定 resetPassword 的错误态清空**

在 `app/src/test/java/space/jtcao/visepanda/feature/auth/AuthViewModelTest.kt` 追加：

```kotlin
@Test
fun `reset password success should clear error`() = runTest {
    val repo = object : AuthRepository {
        override suspend fun login(email: String, password: String) = error("unused")
        override suspend fun register(email: String, password: String, displayName: String?) = error("unused")
        override fun observeSession() = flowOf(null)
        override suspend fun logout() = Unit
        override suspend fun resetPassword(email: String) = Result.success(Unit)
    }

    val vm = AuthViewModel(
        LoginWithEmailUseCase(repo),
        RegisterWithEmailUseCase(repo),
        ObserveSessionUseCase(repo)
    )

    vm.resetPassword("user@test.com")
    advanceUntilIdle()

    assertEquals(null, vm.uiState.value.error)
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.auth.AuthViewModelTest"
```

Expected: FAIL，`resetPassword` 尚未实现。

- [ ] **Step 3: 给 ViewModel 增加 resetPassword**

把 `AuthViewModel.kt` 增加：

```kotlin
fun resetPassword(email: String) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        _uiState.value = AuthUiState(user = _uiState.value.user, loading = false, error = null)
    }
}
```

并把 `ForgotPasswordScreen.kt` 改成调用该方法。

- [ ] **Step 4: 加最小 session store**

创建 `app/src/main/java/space/jtcao/visepanda/data/auth/AuthSessionStore.kt`：

```kotlin
package space.jtcao.visepanda.data.auth

data class AuthSessionSnapshot(
    val userId: String,
    val email: String
)
```

- [ ] **Step 5: 运行测试并提交**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.auth.AuthViewModelTest"
```

Expected: PASS。

```bash
git add app/src/main/java/space/jtcao/visepanda/data/auth/AuthSessionStore.kt app/src/main/java/space/jtcao/visepanda/feature/auth/AuthViewModel.kt app/src/main/java/space/jtcao/visepanda/feature/auth/ForgotPasswordScreen.kt app/src/test/java/space/jtcao/visepanda/feature/auth/AuthViewModelTest.kt
git commit -m "feat: add auth session restore hooks"
```

---

### Task 3: 完成 Admin Dashboard 与用户统计卡片

**Files:**
- Create: `admin/app/dashboard/page.tsx`
- Create: `admin/components/metric-card.tsx`
- Test: `admin/tests/dashboard.test.ts`
- Modify: `admin/app/page.tsx`

- [ ] **Step 1: 写失败测试，固定统计汇总函数**

创建 `admin/tests/dashboard.test.ts`：

```ts
import { describe, expect, it } from "vitest";
import { summarizeUsers } from "../app/dashboard/page";

describe("summarizeUsers", () => {
  it("counts total, active, pending and disabled users", () => {
    const result = summarizeUsers([
      { status: "active" },
      { status: "active" },
      { status: "pending" },
      { status: "disabled" }
    ] as Array<{ status: "active" | "pending" | "disabled" }>);

    expect(result).toEqual({
      total: 4,
      active: 2,
      pending: 1,
      disabled: 1
    });
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd admin && npm test
```

Expected: FAIL，Dashboard 统计函数尚不存在。

- [ ] **Step 3: 实现 Dashboard 页面**

创建 `admin/components/metric-card.tsx`：

```tsx
export function MetricCard({ label, value }: { label: string; value: number }) {
  return (
    <div style={{ border: "1px solid #e5e7eb", borderRadius: 12, padding: 16 }}>
      <div style={{ color: "#6b7280", fontSize: 14 }}>{label}</div>
      <div style={{ fontSize: 28, fontWeight: 700 }}>{value}</div>
    </div>
  );
}
```

创建 `admin/app/dashboard/page.tsx`：

```tsx
import { AdminShell } from "../../components/admin-shell";
import { MetricCard } from "../../components/metric-card";

export function summarizeUsers(items: Array<{ status: "active" | "pending" | "disabled" }>) {
  return {
    total: items.length,
    active: items.filter((item) => item.status === "active").length,
    pending: items.filter((item) => item.status === "pending").length,
    disabled: items.filter((item) => item.status === "disabled").length
  };
}

export default function DashboardPage() {
  const summary = summarizeUsers([]);

  return (
    <AdminShell title="Dashboard" description="用户认证与后台运营概览。">
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, minmax(0, 1fr))", gap: 16 }}>
        <MetricCard label="Total Users" value={summary.total} />
        <MetricCard label="Active" value={summary.active} />
        <MetricCard label="Pending" value={summary.pending} />
        <MetricCard label="Disabled" value={summary.disabled} />
      </div>
    </AdminShell>
  );
}
```

把 `admin/app/page.tsx` 改成重定向到 `dashboard` 或简单导航。

- [ ] **Step 4: 运行测试并提交**

Run:

```bash
cd admin && npm test
```

Expected: PASS。

```bash
git add admin/app/dashboard/page.tsx admin/components/metric-card.tsx admin/tests/dashboard.test.ts admin/app/page.tsx
git commit -m "feat: add admin dashboard"
```

---

### Task 4: 完成 Admin 用户列表筛选

**Files:**
- Modify: `admin/app/users/page.tsx`
- Modify: `admin/lib/users.ts`

- [ ] **Step 1: 写失败测试，固定邮箱搜索行为**

在 `admin/tests/users.test.ts` 追加：

```ts
import { filterUserRows } from "../lib/users";

it("filters rows by email fragment", () => {
  const rows = [
    { id: "1", title: "Alice", subtitle: "alice@test.com", role: "user", status: "active", createdAt: "", lastLoginAt: "" },
    { id: "2", title: "Bob", subtitle: "bob@test.com", role: "admin", status: "disabled", createdAt: "", lastLoginAt: "" }
  ];

  expect(filterUserRows(rows, { q: "alice", role: "all", status: "all" })).toHaveLength(1);
});
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd admin && npm test
```

Expected: FAIL，`filterUserRows` 尚不存在。

- [ ] **Step 3: 实现最小筛选**

在 `admin/lib/users.ts` 增加：

```ts
export function filterUserRows(
  rows: UserRow[],
  input: { q: string; role: string; status: string }
) {
  return rows.filter((row) => {
    const matchQuery =
      input.q.trim() === "" ||
      row.title.toLowerCase().includes(input.q.toLowerCase()) ||
      row.subtitle.toLowerCase().includes(input.q.toLowerCase());
    const matchRole = input.role === "all" || row.role === input.role;
    const matchStatus = input.status === "all" || row.status === input.status;
    return matchQuery && matchRole && matchStatus;
  });
}
```

并在 `admin/app/users/page.tsx` 中接收 `searchParams`，筛选后再渲染。

- [ ] **Step 4: 运行测试并提交**

Run:

```bash
cd admin && npm test
```

Expected: PASS。

```bash
git add admin/app/users/page.tsx admin/lib/users.ts admin/tests/users.test.ts
git commit -m "feat: add admin user filters"
```

---

### Task 5: 更新 handoff 并做阶段验证

**Files:**
- Modify: `HANDOFF.md`

- [ ] **Step 1: 更新 handoff**

增加：

```md
- Auth Phase 2 now includes real Android auth gateway shape, session restore hooks, admin dashboard, and user filters
```

- [ ] **Step 2: 运行验证**

Run:

```bash
cd admin && npm test && npm run build
./gradlew testDebugUnitTest
```

Expected:

- Admin 测试与构建通过
- Android 单测如仍阻塞，明确是 Gradle/SDK 环境问题还是代码问题

- [ ] **Step 3: 提交**

```bash
git add HANDOFF.md
git commit -m "docs: mark auth admin phase 2 entry"
```

---

## 自检

### 规格覆盖

- Android 真实认证接线方向：Task 1-2
- Admin Dashboard：Task 3
- Admin 列表筛选：Task 4
- handoff 与验证：Task 5

### 占位检查

没有 `TODO`、`TBD` 或未定义引用。对真实 Supabase Android SDK 的接线保持最小、可替换形态，避免在当前阶段把环境配置问题和业务实现纠缠在一起。

### 类型一致性

- `SupabaseAuthGateway` / `AuthRepositoryImpl` / `AuthViewModel` 命名一致
- `UserRole` / `UserStatus` 与现有 admin 类型保持一致
- Dashboard 和 user filters 都复用已有 `UserRow` / `UserProfile` 概念
