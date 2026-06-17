import Link from "next/link";
import { notFound } from "next/navigation";
import { AdminShell } from "../../../components/admin-shell";
import { UserRoleForm } from "../../../components/user-role-form";
import { createServerSupabaseAdminClient, isSupabaseServerConfigured } from "../../../lib/supabase/server";
import type { UserProfile } from "../../../lib/types";

async function loadUser(id: string) {
  if (!isSupabaseServerConfigured()) {
    return {
      user: null,
      error: "Supabase 环境变量未配置，暂时无法加载用户详情。"
    };
  }

  try {
    const supabase = createServerSupabaseAdminClient();
    const { data, error } = await supabase.from("user_profiles").select("*").eq("id", id).maybeSingle();

    if (error) {
      return {
        user: null,
        error: `加载用户详情失败：${error.message}`
      };
    }

    if (!data) {
      notFound();
    }

    return {
      user: data as UserProfile,
      error: null
    };
  } catch (error) {
    return {
      user: null,
      error: error instanceof Error ? error.message : "加载用户详情时出现未知错误。"
    };
  }
}

export default async function UserDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const { user, error } = await loadUser(id);

  return (
    <AdminShell title="用户详情" description="查看并更新单个用户的名称、头像、角色与状态。">
      <div style={{ display: "grid", gap: 20 }}>
        <Link href="/users">返回用户列表</Link>

        {error ? (
          <div
            style={{
              borderRadius: 12,
              padding: "12px 16px",
              border: "1px solid #fecaca",
              background: "#fef2f2",
              color: "#991b1b"
            }}
          >
            {error}
          </div>
        ) : null}

        {user ? (
          <>
            <div
              style={{
                display: "grid",
                gap: 10,
                padding: 20,
                borderRadius: 16,
                border: "1px solid #e5e7eb",
                background: "#f8fafc"
              }}
            >
              <div>
                <strong>ID：</strong>
                <span>{user.id}</span>
              </div>
              <div>
                <strong>Email：</strong>
                <span>{user.email}</span>
              </div>
              <div>
                <strong>创建时间：</strong>
                <span>{user.created_at}</span>
              </div>
              <div>
                <strong>最近登录：</strong>
                <span>{user.last_login_at ?? "从未登录"}</span>
              </div>
            </div>

            <UserRoleForm user={user} />
          </>
        ) : null}
      </div>
    </AdminShell>
  );
}
