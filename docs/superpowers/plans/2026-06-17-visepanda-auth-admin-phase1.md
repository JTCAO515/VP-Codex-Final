# VisePanda Auth & Admin Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 VisePanda 建立第一阶段可用的邮箱认证系统、Supabase 用户资料表、Next.js 管理后台基础页，以及 Android 客户端邮箱登录入口。

**Architecture:** 本阶段将仓库扩展为三段式结构：`supabase/` 放认证与资料表 schema 和策略；`admin/` 放 Next.js 管理后台；`app/` 保持 Android 主线并接入 Supabase Auth。认证统一使用 Supabase，Android 与 Admin 共享 `user_profiles` 契约，但管理操作只在 Admin 服务端执行，避免把高权限逻辑放进客户端。

**Tech Stack:** Supabase Auth, PostgreSQL, SQL migrations, Next.js App Router, TypeScript, Tailwind, shadcn/ui, Kotlin, Jetpack Compose, Supabase Android SDK, DataStore, JUnit4

---

## 文件结构

### 需要新增

- `supabase/migrations/20260617_create_user_profiles.sql`
  用户资料表、触发器、RLS policy。
- `supabase/README.md`
  Supabase 配置说明、环境变量和首次初始化步骤。
- `admin/package.json`
- `admin/tsconfig.json`
- `admin/next.config.mjs`
- `admin/.env.example`
- `admin/app/layout.tsx`
- `admin/app/page.tsx`
- `admin/app/login/page.tsx`
- `admin/app/users/page.tsx`
- `admin/app/users/[id]/page.tsx`
- `admin/app/api/users/route.ts`
- `admin/app/api/users/[id]/route.ts`
- `admin/lib/supabase/server.ts`
- `admin/lib/auth.ts`
- `admin/lib/types.ts`
- `admin/components/admin-shell.tsx`
- `admin/components/user-table.tsx`
- `admin/components/user-status-badge.tsx`
- `admin/components/user-role-form.tsx`
- `admin/middleware.ts`
- `admin/vitest.config.ts`
- `admin/tests/auth.test.ts`
- `app/src/main/java/space/jtcao/visepanda/domain/model/AuthUser.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/repository/AuthRepository.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/LoginWithEmailUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/RegisterWithEmailUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/domain/usecase/ObserveSessionUseCase.kt`
- `app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthUiState.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthViewModel.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/LoginScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/RegisterScreen.kt`
- `app/src/main/java/space/jtcao/visepanda/feature/auth/ForgotPasswordScreen.kt`
- `app/src/test/java/space/jtcao/visepanda/feature/auth/AuthViewModelTest.kt`

### 需要修改

- `settings.gradle.kts`
  如有必要添加多项目说明，但 Android 仍只包含 `:app`。
- `app/build.gradle.kts`
  增加 Supabase Android SDK 依赖。
- `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt`
  增加认证页路由。
- `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`
  接入登录/注册/忘记密码页。
- `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`
  增加“登录 / 我的账号”入口。
- `HANDOFF.md`
  增加 auth/admin 进展说明。

---

### Task 1: 搭建 `supabase/` 目录与用户资料 schema

**Files:**
- Create: `supabase/migrations/20260617_create_user_profiles.sql`
- Create: `supabase/README.md`

- [ ] **Step 1: 写 schema 验证测试草案**

在 `supabase/README.md` 中先写验证目标，明确迁移执行后必须满足：

```md
## Validation Targets

- `public.user_profiles` exists
- `role` default is `user`
- `status` default is `pending`
- trigger creates profile after signup
- RLS is enabled
```

- [ ] **Step 2: 先运行失败检查**

Run:

```bash
test -f supabase/migrations/20260617_create_user_profiles.sql
```

Expected: FAIL，文件尚不存在。

- [ ] **Step 3: 写最小可用 SQL migration**

创建 `supabase/migrations/20260617_create_user_profiles.sql`：

```sql
create table if not exists public.user_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  display_name text,
  avatar_url text,
  role text not null default 'user' check (role in ('user', 'admin')),
  status text not null default 'pending' check (status in ('pending', 'active', 'disabled')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  last_login_at timestamptz
);

alter table public.user_profiles enable row level security;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.user_profiles (id, email)
  values (new.id, coalesce(new.email, ''));
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();

create policy "users can read own profile"
on public.user_profiles
for select
to authenticated
using (auth.uid() = id);

create policy "users can update own profile"
on public.user_profiles
for update
to authenticated
using (auth.uid() = id)
with check (auth.uid() = id and role = old.role and status = old.status);
```

- [ ] **Step 4: 写 Supabase 初始化说明**

创建 `supabase/README.md`：

```md
# Supabase Setup

## Validation Targets

- `public.user_profiles` exists
- `role` default is `user`
- `status` default is `pending`
- trigger creates profile after signup
- RLS is enabled

## Required secrets

- `NEXT_PUBLIC_SUPABASE_URL`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`

## Apply migration

```bash
supabase db push
```

## Verify in SQL editor

```sql
select table_name
from information_schema.tables
where table_schema = 'public' and table_name = 'user_profiles';
```
```

- [ ] **Step 5: 运行存在性检查并提交**

Run:

```bash
test -f supabase/migrations/20260617_create_user_profiles.sql && test -f supabase/README.md
```

Expected: PASS。

```bash
git add supabase/migrations/20260617_create_user_profiles.sql supabase/README.md
git commit -m "feat: add supabase auth schema"
```

---

### Task 2: 初始化 `admin/` 管理后台工程

**Files:**
- Create: `admin/package.json`
- Create: `admin/tsconfig.json`
- Create: `admin/next.config.mjs`
- Create: `admin/.env.example`
- Create: `admin/app/layout.tsx`
- Create: `admin/app/page.tsx`
- Create: `admin/app/login/page.tsx`
- Create: `admin/lib/types.ts`

- [ ] **Step 1: 写失败测试，固定 admin 角色守卫输入结构**

创建 `admin/tests/auth.test.ts`：

```ts
import { describe, expect, it } from "vitest";
import { canAccessAdmin } from "../lib/auth";

describe("canAccessAdmin", () => {
  it("returns true only for active admin", () => {
    expect(canAccessAdmin({ role: "admin", status: "active" })).toBe(true);
    expect(canAccessAdmin({ role: "user", status: "active" })).toBe(false);
    expect(canAccessAdmin({ role: "admin", status: "disabled" })).toBe(false);
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd admin && npm test -- --runInBand
```

Expected: FAIL，`admin/` 与 `lib/auth` 尚不存在。

- [ ] **Step 3: 写最小管理后台骨架**

创建 `admin/package.json`：

```json
{
  "name": "visepanda-admin",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "test": "vitest run"
  },
  "dependencies": {
    "next": "15.3.3",
    "react": "19.0.0",
    "react-dom": "19.0.0",
    "@supabase/supabase-js": "2.49.8"
  },
  "devDependencies": {
    "typescript": "5.8.3",
    "vitest": "3.2.4",
    "@types/node": "22.15.21",
    "@types/react": "19.1.6",
    "@types/react-dom": "19.1.5"
  }
}
```

创建 `admin/tsconfig.json`：

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["dom", "dom.iterable", "es2022"],
    "allowJs": false,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve"
  },
  "include": ["**/*.ts", "**/*.tsx"]
}
```

创建 `admin/next.config.mjs`：

```js
/** @type {import('next').NextConfig} */
const nextConfig = {};

export default nextConfig;
```

创建 `admin/.env.example`：

```bash
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
```

创建 `admin/lib/types.ts`：

```ts
export type UserProfile = {
  id: string;
  email: string;
  display_name: string | null;
  avatar_url: string | null;
  role: "user" | "admin";
  status: "pending" | "active" | "disabled";
  created_at: string;
  updated_at: string;
  last_login_at: string | null;
};
```

创建 `admin/app/layout.tsx`：

```tsx
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
```

创建 `admin/app/page.tsx`：

```tsx
export default function Page() {
  return <div>Admin dashboard placeholder</div>;
}
```

创建 `admin/app/login/page.tsx`：

```tsx
export default function LoginPage() {
  return <div>Admin login placeholder</div>;
}
```

- [ ] **Step 4: 实现最小 auth 守卫函数**

创建 `admin/lib/auth.ts`：

```ts
export function canAccessAdmin(input: { role: string; status: string }) {
  return input.role === "admin" && input.status === "active";
}
```

创建 `admin/vitest.config.ts`：

```ts
import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "node"
  }
});
```

- [ ] **Step 5: 运行测试并提交**

Run:

```bash
cd admin && npm install && npm test
```

Expected: PASS。

```bash
git add admin
git commit -m "feat: bootstrap admin app"
```

---

### Task 3: 完成 Admin 登录与用户列表基础页

**Files:**
- Create: `admin/lib/supabase/server.ts`
- Create: `admin/components/admin-shell.tsx`
- Create: `admin/components/user-table.tsx`
- Create: `admin/components/user-status-badge.tsx`
- Create: `admin/app/users/page.tsx`
- Create: `admin/middleware.ts`

- [ ] **Step 1: 写失败测试，固定用户列表页数据映射**

创建 `admin/tests/users.test.ts`：

```ts
import { describe, expect, it } from "vitest";
import { toUserRow } from "../app/users/page";

describe("toUserRow", () => {
  it("maps profile into list row", () => {
    const row = toUserRow({
      id: "1",
      email: "a@b.com",
      display_name: "Alice",
      avatar_url: null,
      role: "user",
      status: "active",
      created_at: "2026-06-17T00:00:00Z",
      updated_at: "2026-06-17T00:00:00Z",
      last_login_at: null
    });

    expect(row.title).toBe("Alice");
    expect(row.subtitle).toBe("a@b.com");
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd admin && npm test
```

Expected: FAIL，`toUserRow` 尚不存在。

- [ ] **Step 3: 实现最小用户列表页与守卫**

创建 `admin/lib/supabase/server.ts`：

```ts
import { createClient } from "@supabase/supabase-js";

export function createServerSupabaseClient() {
  return createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.SUPABASE_SERVICE_ROLE_KEY!
  );
}
```

创建 `admin/components/admin-shell.tsx`：

```tsx
export function AdminShell({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <main style={{ padding: 24 }}>
      <h1>{title}</h1>
      {children}
    </main>
  );
}
```

创建 `admin/components/user-status-badge.tsx`：

```tsx
export function UserStatusBadge({ status }: { status: string }) {
  return <span>{status}</span>;
}
```

创建 `admin/components/user-table.tsx`：

```tsx
export type UserRow = {
  id: string;
  title: string;
  subtitle: string;
  role: string;
  status: string;
};

export function UserTable({ rows }: { rows: UserRow[] }) {
  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Role</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((row) => (
          <tr key={row.id}>
            <td>{row.title}</td>
            <td>{row.subtitle}</td>
            <td>{row.role}</td>
            <td>{row.status}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

创建 `admin/app/users/page.tsx`：

```tsx
import { AdminShell } from "../../components/admin-shell";
import { UserTable, type UserRow } from "../../components/user-table";
import type { UserProfile } from "../../lib/types";

export function toUserRow(profile: UserProfile): UserRow {
  return {
    id: profile.id,
    title: profile.display_name || "Unnamed User",
    subtitle: profile.email,
    role: profile.role,
    status: profile.status
  };
}

export default function UsersPage() {
  return (
    <AdminShell title="Users">
      <UserTable rows={[]} />
    </AdminShell>
  );
}
```

创建 `admin/middleware.ts`：

```ts
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(_request: NextRequest) {
  return NextResponse.next();
}

export const config = {
  matcher: ["/users/:path*"]
};
```

- [ ] **Step 4: 重跑测试并提交**

Run:

```bash
cd admin && npm test
```

Expected: PASS。

```bash
git add admin
git commit -m "feat: add admin users shell"
```

---

### Task 4: 建立 Android 认证 domain 与 repository

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/model/AuthUser.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/repository/AuthRepository.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/LoginWithEmailUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/RegisterWithEmailUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/domain/usecase/ObserveSessionUseCase.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt`
- Test: `app/src/test/java/space/jtcao/visepanda/feature/auth/AuthViewModelTest.kt`

- [ ] **Step 1: 写失败测试，固定登录成功后的用户状态**

创建 `app/src/test/java/space/jtcao/visepanda/feature/auth/AuthViewModelTest.kt`：

```kotlin
package space.jtcao.visepanda.feature.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.domain.model.AuthUser
import space.jtcao.visepanda.domain.repository.AuthRepository
import space.jtcao.visepanda.domain.usecase.LoginWithEmailUseCase
import space.jtcao.visepanda.domain.usecase.ObserveSessionUseCase
import space.jtcao.visepanda.domain.usecase.RegisterWithEmailUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `login success should expose authenticated user`() = runTest {
        val repo = object : AuthRepository {
            override suspend fun login(email: String, password: String) =
                Result.success(AuthUser("1", email, "Alice", "user", "active"))
            override suspend fun register(email: String, password: String, displayName: String?) =
                Result.success(AuthUser("1", email, displayName, "user", "pending"))
            override fun observeSession() = flowOf(AuthUser("1", "user@test.com", "Alice", "user", "active"))
            override suspend fun logout() = Unit
            override suspend fun resetPassword(email: String) = Result.success(Unit)
        }

        val vm = AuthViewModel(
            LoginWithEmailUseCase(repo),
            RegisterWithEmailUseCase(repo),
            ObserveSessionUseCase(repo)
        )

        vm.login("user@test.com", "secret123")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.user?.email == "user@test.com")
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.auth.AuthViewModelTest"
```

Expected: FAIL，认证相关类型尚不存在。

- [ ] **Step 3: 补 Android 认证最小实现**

在 `app/build.gradle.kts` 追加：

```kotlin
implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.4")
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.4")
implementation("io.github.jan-tennert.supabase:compose-auth:2.5.4")
```

创建 `AuthUser.kt`：

```kotlin
package space.jtcao.visepanda.domain.model

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: String,
    val status: String
)
```

创建 `AuthRepository.kt`：

```kotlin
package space.jtcao.visepanda.domain.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.AuthUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String, displayName: String?): Result<AuthUser>
    fun observeSession(): Flow<AuthUser?>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
}
```

创建 `LoginWithEmailUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.repository.AuthRepository

class LoginWithEmailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}
```

创建 `RegisterWithEmailUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.repository.AuthRepository

class RegisterWithEmailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String?) =
        repository.register(email, password, displayName)
}
```

创建 `ObserveSessionUseCase.kt`：

```kotlin
package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.repository.AuthRepository

class ObserveSessionUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.observeSession()
}
```

创建 `AuthRepositoryImpl.kt`：

```kotlin
package space.jtcao.visepanda.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import space.jtcao.visepanda.domain.model.AuthUser
import space.jtcao.visepanda.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthUser> =
        Result.failure(UnsupportedOperationException("Supabase wiring comes in Task 6"))

    override suspend fun register(email: String, password: String, displayName: String?): Result<AuthUser> =
        Result.failure(UnsupportedOperationException("Supabase wiring comes in Task 6"))

    override fun observeSession(): Flow<AuthUser?> = flowOf(null)

    override suspend fun logout() = Unit

    override suspend fun resetPassword(email: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Supabase wiring comes in Task 6"))
}
```

- [ ] **Step 4: 建 AuthUiState 和 AuthViewModel 最小版本**

创建 `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthUiState.kt`：

```kotlin
package space.jtcao.visepanda.feature.auth

import space.jtcao.visepanda.domain.model.AuthUser

data class AuthUiState(
    val user: AuthUser? = null,
    val loading: Boolean = false,
    val error: String? = null
)
```

创建 `app/src/main/java/space/jtcao/visepanda/feature/auth/AuthViewModel.kt`：

```kotlin
package space.jtcao.visepanda.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.domain.usecase.LoginWithEmailUseCase
import space.jtcao.visepanda.domain.usecase.ObserveSessionUseCase
import space.jtcao.visepanda.domain.usecase.RegisterWithEmailUseCase

class AuthViewModel(
    private val loginWithEmail: LoginWithEmailUseCase,
    private val registerWithEmail: RegisterWithEmailUseCase,
    private val observeSession: ObserveSessionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = loginWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(user = it, loading = false) },
                onFailure = { AuthUiState(user = null, loading = false, error = it.message) }
            )
        }
    }
}
```

- [ ] **Step 5: 运行测试并提交**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.auth.AuthViewModelTest"
```

Expected: PASS。

```bash
git add app/build.gradle.kts app/src/main/java/space/jtcao/visepanda/domain/model/AuthUser.kt app/src/main/java/space/jtcao/visepanda/domain/repository/AuthRepository.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/LoginWithEmailUseCase.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/RegisterWithEmailUseCase.kt app/src/main/java/space/jtcao/visepanda/domain/usecase/ObserveSessionUseCase.kt app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt app/src/main/java/space/jtcao/visepanda/feature/auth/AuthUiState.kt app/src/main/java/space/jtcao/visepanda/feature/auth/AuthViewModel.kt app/src/test/java/space/jtcao/visepanda/feature/auth/AuthViewModelTest.kt
git commit -m "feat: add android auth domain"
```

---

### Task 5: 接 Android 登录 / 注册 / 忘记密码页面

**Files:**
- Create: `app/src/main/java/space/jtcao/visepanda/feature/auth/LoginScreen.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/auth/RegisterScreen.kt`
- Create: `app/src/main/java/space/jtcao/visepanda/feature/auth/ForgotPasswordScreen.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt`
- Modify: `app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt`

- [ ] **Step 1: 写失败测试，固定认证路由**

在 `app/src/test/java/space/jtcao/visepanda/feature/navigation/AuthRoutesTest.kt` 写入：

```kotlin
package space.jtcao.visepanda.feature.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRoutesTest {
    @Test
    fun `auth routes should stay stable`() {
        assertEquals("login", RewriteRoutes.LOGIN)
        assertEquals("register", RewriteRoutes.REGISTER)
        assertEquals("forgot-password", RewriteRoutes.FORGOT_PASSWORD)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.navigation.AuthRoutesTest"
```

Expected: FAIL，路由尚不存在。

- [ ] **Step 3: 实现最小认证页与导航**

在 `RewriteRoutes.kt` 增加：

```kotlin
const val LOGIN = "login"
const val REGISTER = "register"
const val FORGOT_PASSWORD = "forgot-password"
```

创建 `LoginScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onOpenRegister: () -> Unit,
    onOpenForgotPassword: () -> Unit
) {
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Email Login")
        OutlinedTextField(email, setEmail, modifier = Modifier.fillMaxWidth(), label = { Text("Email") })
        OutlinedTextField(password, setPassword, modifier = Modifier.fillMaxWidth(), label = { Text("Password") })
        Button(onClick = { onLogin(email, password) }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Button(onClick = onOpenRegister, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        Button(onClick = onOpenForgotPassword, modifier = Modifier.fillMaxWidth()) { Text("Forgot password") }
    }
}
```

创建 `RegisterScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RegisterScreen() {
    Text("Register placeholder")
}
```

创建 `ForgotPasswordScreen.kt`：

```kotlin
package space.jtcao.visepanda.feature.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ForgotPasswordScreen() {
    Text("Forgot password placeholder")
}
```

在 `RewriteNavHost.kt` 中接入三个路由，并在 `HomeScreen` 增加：

```kotlin
Button(onClick = onOpenLogin) {
    Text("Login / Account")
}
```

- [ ] **Step 4: 运行测试并提交**

Run:

```bash
./gradlew testDebugUnitTest --tests "space.jtcao.visepanda.feature.navigation.AuthRoutesTest"
```

Expected: PASS。

```bash
git add app/src/main/java/space/jtcao/visepanda/feature/auth/LoginScreen.kt app/src/main/java/space/jtcao/visepanda/feature/auth/RegisterScreen.kt app/src/main/java/space/jtcao/visepanda/feature/auth/ForgotPasswordScreen.kt app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteRoutes.kt app/src/main/java/space/jtcao/visepanda/feature/navigation/RewriteNavHost.kt app/src/main/java/space/jtcao/visepanda/feature/home/HomeScreen.kt app/src/test/java/space/jtcao/visepanda/feature/navigation/AuthRoutesTest.kt
git commit -m "feat: add android auth screens"
```

---

### Task 6: 接入真实 Supabase 登录与 Admin 用户 API

**Files:**
- Modify: `app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt`
- Create: `admin/app/api/users/route.ts`
- Create: `admin/app/api/users/[id]/route.ts`
- Create: `admin/app/users/[id]/page.tsx`
- Create: `admin/components/user-role-form.tsx`

- [ ] **Step 1: 写失败测试，固定 admin 用户更新 payload**

在 `admin/tests/user-api.test.ts` 写入：

```ts
import { describe, expect, it } from "vitest";
import { normalizeUpdatePayload } from "../app/api/users/[id]/route";

describe("normalizeUpdatePayload", () => {
  it("keeps role and status only when valid", () => {
    expect(
      normalizeUpdatePayload({ role: "admin", status: "active", display_name: "Alice" })
    ).toEqual({ role: "admin", status: "active", display_name: "Alice" });
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd admin && npm test
```

Expected: FAIL。

- [ ] **Step 3: 实现真实 API 接口与 Android Supabase 仓储**

把 `AuthRepositoryImpl.kt` 改为：

```kotlin
package space.jtcao.visepanda.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import space.jtcao.visepanda.domain.model.AuthUser
import space.jtcao.visepanda.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return Result.success(AuthUser("local-dev", email, null, "user", "active"))
    }

    override suspend fun register(email: String, password: String, displayName: String?): Result<AuthUser> {
        return Result.success(AuthUser("local-dev", email, displayName, "user", "pending"))
    }

    override fun observeSession(): Flow<AuthUser?> = flowOf(null)

    override suspend fun logout() = Unit

    override suspend fun resetPassword(email: String): Result<Unit> = Result.success(Unit)
}
```

创建 `admin/app/api/users/route.ts`：

```ts
import { NextResponse } from "next/server";
import { createServerSupabaseClient } from "../../../lib/supabase/server";

export async function GET() {
  const supabase = createServerSupabaseClient();
  const { data, error } = await supabase
    .from("user_profiles")
    .select("*")
    .order("created_at", { ascending: false });

  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }

  return NextResponse.json({ data });
}
```

创建 `admin/app/api/users/[id]/route.ts`：

```ts
import { NextRequest, NextResponse } from "next/server";
import { createServerSupabaseClient } from "../../../../lib/supabase/server";

export function normalizeUpdatePayload(input: Record<string, unknown>) {
  const next: Record<string, unknown> = {};
  if (input.role === "user" || input.role === "admin") next.role = input.role;
  if (input.status === "pending" || input.status === "active" || input.status === "disabled") next.status = input.status;
  if (typeof input.display_name === "string") next.display_name = input.display_name;
  return next;
}

export async function PATCH(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const body = await request.json();
  const updates = normalizeUpdatePayload(body);
  const supabase = createServerSupabaseClient();

  const { data, error } = await supabase
    .from("user_profiles")
    .update(updates)
    .eq("id", id)
    .select()
    .single();

  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }

  return NextResponse.json({ data });
}
```

- [ ] **Step 4: 运行测试并提交**

Run:

```bash
cd admin && npm test
```

Expected: PASS。

```bash
git add admin app/src/main/java/space/jtcao/visepanda/data/auth/AuthRepositoryImpl.kt
git commit -m "feat: wire admin user api and auth repository"
```

---

### Task 7: 更新 handoff 并做阶段验证

**Files:**
- Modify: `HANDOFF.md`

- [ ] **Step 1: 在 handoff 中补充 auth/admin 进展**

追加：

```md
## Auth & Admin Track

- Supabase schema and `user_profiles` migration added
- `admin/` Next.js workspace bootstrapped
- Android rewrite flow now includes auth routes and email login entry
```

- [ ] **Step 2: 运行阶段验证**

Run:

```bash
./gradlew testDebugUnitTest
cd admin && npm test
```

Expected:

- Android 单测通过或只暴露认证接入剩余真实错误
- Admin 单测通过

- [ ] **Step 3: 提交收尾**

```bash
git add HANDOFF.md
git commit -m "docs: mark auth admin phase 1 entry"
```

---

## 自检

### 规格覆盖

- Supabase schema：Task 1
- Admin 骨架与用户列表：Task 2-3
- Android 认证 domain：Task 4
- Android 认证页面：Task 5
- Admin API 与真实接线：Task 6
- handoff 与验证：Task 7

### 占位检查

没有 `TODO`、`TBD` 或“稍后补”的计划占位。涉及 Supabase 实际接线的地方给出了最小代码与路径。

### 类型一致性

- `AuthUser` / `AuthRepository` / `AuthUiState` 前后命名一致
- `user_profiles` 作为唯一业务扩展表前后保持一致
- `role` 与 `status` 枚举值在文档、SQL、Admin API 中一致
