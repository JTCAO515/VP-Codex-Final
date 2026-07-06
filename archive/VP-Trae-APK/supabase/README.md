# Supabase Setup

本目录用于保存 VisePanda Auth & Admin Phase 1 的数据库契约，包括 `user_profiles` 表、触发器、RLS policy 与初始化说明。

## Validation Targets

- `public.user_profiles` exists
- `role` default is `user`
- `status` default is `pending`
- trigger creates profile after signup
- `updated_at` is refreshed on update
- RLS is enabled

## 最小失败存在性检查

按计划先做最小存在性检查，确认迁移文件在落代码前不存在：

```bash
test -f supabase/migrations/20260617_create_user_profiles.sql
```

预期：返回非 0，表示文件尚未创建。

落代码后再执行：

```bash
test -f supabase/migrations/20260617_create_user_profiles.sql && test -f supabase/README.md
```

预期：返回 0，表示 Task 1 所需文件已就位。

## Required secrets

- `NEXT_PUBLIC_SUPABASE_URL`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_REDIRECT_SCHEME`
- `SUPABASE_REDIRECT_HOST`

## Android Auth 配置

Android 端会从 Gradle property 或环境变量读取以下配置，并注入到 `BuildConfig` 与 `AndroidManifest.xml`：

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_REDIRECT_SCHEME`
- `SUPABASE_REDIRECT_HOST`

默认 deeplink 目标是：

```text
space.jtcao.visepanda://auth-callback
```

请同时在 Supabase Dashboard 的 Auth Redirect URLs 中加入同样的地址，确保以下场景能回跳到 App：

- 注册确认邮件
- 忘记密码邮件
- 其他需要 deeplink 的 Auth 回调

## 初始化

1. 安装并登录 Supabase CLI。
2. 在仓库根目录执行迁移。
3. 在 Supabase 项目中启用 Email Auth。

常用命令：

```bash
supabase login
supabase start
supabase db push
```

如果是远端项目，先执行 `supabase link --project-ref <your-project-ref>`，再执行 `supabase db push`。

## Migration 内容

本次迁移会创建：

- `public.user_profiles` 表
- `public.handle_new_user()` 注册后自动建档触发器函数
- `public.handle_user_profile_updated_at()` 更新时间触发器函数
- 用户读取/更新本人资料的 RLS policy

## Verify in SQL editor

确认表存在：

```sql
select table_name
from information_schema.tables
where table_schema = 'public' and table_name = 'user_profiles';
```

确认默认值：

```sql
select
  column_name,
  column_default
from information_schema.columns
where table_schema = 'public'
  and table_name = 'user_profiles'
  and column_name in ('role', 'status');
```

确认 RLS 已开启：

```sql
select relrowsecurity
from pg_class
where oid = 'public.user_profiles'::regclass;
```

确认触发器已存在：

```sql
select trigger_name, event_object_table
from information_schema.triggers
where trigger_schema = 'public'
   or event_object_schema = 'auth';
```
