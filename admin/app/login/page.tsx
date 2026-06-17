import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { ADMIN_AUTH_COOKIE, canAccessAdmin } from "../../lib/auth";
import {
  createServerSupabaseAdminClient,
  createServerSupabaseClient,
  isSupabaseServerConfigured
} from "../../lib/supabase/server";
import type { UserProfile } from "../../lib/types";

type LoginPageProps = {
  searchParams: Promise<{
    error?: string;
    from?: string;
  }>;
};

function normalizeRedirectTarget(input?: string) {
  if (!input || !input.startsWith("/") || input.startsWith("//")) {
    return "/users";
  }

  return input;
}

async function loginAction(formData: FormData) {
  "use server";

  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const redirectTo = normalizeRedirectTarget(String(formData.get("redirectTo") ?? ""));

  if (!email || !password) {
    redirect(`/login?error=${encodeURIComponent("请输入邮箱和密码")}&from=${encodeURIComponent(redirectTo)}`);
  }

  if (!isSupabaseServerConfigured()) {
    redirect(`/login?error=${encodeURIComponent("Supabase 环境变量未配置，无法登录")}&from=${encodeURIComponent(redirectTo)}`);
  }

  const authClient = createServerSupabaseClient();
  const adminClient = createServerSupabaseAdminClient();
  const signInResult = await authClient.auth.signInWithPassword({ email, password });

  if (signInResult.error || !signInResult.data.user) {
    redirect(
      `/login?error=${encodeURIComponent(signInResult.error?.message ?? "登录失败")}&from=${encodeURIComponent(redirectTo)}`
    );
  }

  const { data: profile, error: profileError } = await adminClient
    .from("user_profiles")
    .select("*")
    .eq("id", signInResult.data.user.id)
    .maybeSingle();

  if (profileError || !profile) {
    redirect(
      `/login?error=${encodeURIComponent(profileError?.message ?? "未找到管理员资料")}&from=${encodeURIComponent(
        redirectTo
      )}`
    );
  }

  if (!canAccessAdmin(profile as UserProfile)) {
    redirect(`/login?error=${encodeURIComponent("当前账号没有后台访问权限")}&from=${encodeURIComponent(redirectTo)}`);
  }

  const cookieStore = await cookies();
  cookieStore.set(ADMIN_AUTH_COOKIE, signInResult.data.user.id, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/"
  });

  redirect(redirectTo);
}

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const params = await searchParams;
  const error = params.error;
  const redirectTo = normalizeRedirectTarget(params.from);

  return (
    <main
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        background: "#f5f7fb",
        padding: 24
      }}
    >
      <section
        style={{
          width: "100%",
          maxWidth: 420,
          background: "#ffffff",
          borderRadius: 20,
          border: "1px solid #e5e7eb",
          padding: 28,
          boxShadow: "0 20px 50px rgba(15, 23, 42, 0.08)"
        }}
      >
        <h1 style={{ margin: 0, fontSize: 28 }}>管理员登录</h1>
        <p style={{ margin: "12px 0 0", color: "#4b5563", lineHeight: 1.7 }}>
          使用 Supabase 邮箱账号登录。仅状态为 active 的 admin 角色可进入用户列表。
        </p>

        {error ? (
          <div
            style={{
              marginTop: 16,
              borderRadius: 12,
              padding: "12px 14px",
              background: "#fef2f2",
              border: "1px solid #fecaca",
              color: "#991b1b"
            }}
          >
            {error}
          </div>
        ) : null}

        <form action={loginAction} style={{ display: "grid", gap: 16, marginTop: 24 }}>
          <input type="hidden" name="redirectTo" value={redirectTo} />

          <label style={{ display: "grid", gap: 8 }}>
            <span style={{ fontWeight: 600 }}>邮箱</span>
            <input
              type="email"
              name="email"
              placeholder="admin@example.com"
              style={{
                height: 44,
                borderRadius: 12,
                border: "1px solid #d1d5db",
                padding: "0 14px"
              }}
              required
            />
          </label>

          <label style={{ display: "grid", gap: 8 }}>
            <span style={{ fontWeight: 600 }}>密码</span>
            <input
              type="password"
              name="password"
              placeholder="请输入密码"
              style={{
                height: 44,
                borderRadius: 12,
                border: "1px solid #d1d5db",
                padding: "0 14px"
              }}
              required
            />
          </label>

          <button
            type="submit"
            style={{
              height: 46,
              border: "none",
              borderRadius: 12,
              background: "#111827",
              color: "#ffffff",
              fontWeight: 700,
              cursor: "pointer"
            }}
          >
            登录后台
          </button>
        </form>
      </section>
    </main>
  );
}
