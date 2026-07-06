import { AdminShell } from "../../components/admin-shell";
import { UserTable, type UserRow } from "../../components/user-table";
import { createServerSupabaseAdminClient, isSupabaseServerConfigured } from "../../lib/supabase/server";
import { filterUserRows, toUserRow } from "../../lib/users";
import type { UserProfile, UserRole, UserStatus } from "../../lib/types";

type UsersPageProps = {
  searchParams: Promise<{
    q?: string;
    role?: string;
    status?: string;
  }>;
};

function normalizeRole(value?: string): UserRole | "all" {
  if (value === "user" || value === "admin") {
    return value;
  }

  return "all";
}

function normalizeStatus(value?: string): UserStatus | "all" {
  if (value === "pending" || value === "active" || value === "disabled") {
    return value;
  }

  return "all";
}

async function loadUsers() {
  if (!isSupabaseServerConfigured()) {
    return {
      rows: [] as UserRow[],
      error: "Supabase 环境变量未配置，暂时无法加载用户列表。"
    };
  }

  try {
    const supabase = createServerSupabaseAdminClient();
    const { data, error } = await supabase
      .from("user_profiles")
      .select("*")
      .order("created_at", { ascending: false });

    if (error) {
      return {
        rows: [] as UserRow[],
        error: `加载用户列表失败：${error.message}`
      };
    }

    return {
      rows: (data ?? []).map((profile) => toUserRow(profile as UserProfile)),
      error: null
    };
  } catch (error) {
    return {
      rows: [] as UserRow[],
      error: error instanceof Error ? error.message : "加载用户列表时出现未知错误。"
    };
  }
}

export default async function UsersPage({ searchParams }: UsersPageProps) {
  const params = await searchParams;
  const { rows, error } = await loadUsers();
  const filters = {
    q: String(params.q ?? ""),
    role: normalizeRole(params.role),
    status: normalizeStatus(params.status)
  };
  const filteredRows = filterUserRows(rows, filters);
  const isFiltered = filters.q.trim() !== "" || filters.role !== "all" || filters.status !== "all";

  return (
    <AdminShell title="用户列表" description="查看当前 Supabase 用户资料、角色状态与登录概览。">
      <div style={{ display: "grid", gap: 16 }}>
        <form
          method="get"
          style={{
            display: "grid",
            gridTemplateColumns: "minmax(220px, 1.5fr) repeat(2, minmax(140px, 1fr)) auto auto",
            gap: 12,
            alignItems: "end"
          }}
        >
          <label style={{ display: "grid", gap: 8 }}>
            <span style={{ fontSize: 14, fontWeight: 600 }}>搜索用户</span>
            <input
              type="search"
              name="q"
              defaultValue={filters.q}
              placeholder="按姓名或邮箱搜索"
              style={{
                height: 40,
                borderRadius: 12,
                border: "1px solid #d1d5db",
                padding: "0 14px"
              }}
            />
          </label>

          <label style={{ display: "grid", gap: 8 }}>
            <span style={{ fontSize: 14, fontWeight: 600 }}>角色</span>
            <select
              name="role"
              defaultValue={filters.role}
              style={{ height: 40, borderRadius: 12, border: "1px solid #d1d5db", padding: "0 12px" }}
            >
              <option value="all">全部角色</option>
              <option value="admin">管理员</option>
              <option value="user">普通用户</option>
            </select>
          </label>

          <label style={{ display: "grid", gap: 8 }}>
            <span style={{ fontSize: 14, fontWeight: 600 }}>状态</span>
            <select
              name="status"
              defaultValue={filters.status}
              style={{ height: 40, borderRadius: 12, border: "1px solid #d1d5db", padding: "0 12px" }}
            >
              <option value="all">全部状态</option>
              <option value="active">激活</option>
              <option value="pending">待处理</option>
              <option value="disabled">已禁用</option>
            </select>
          </label>

          <button
            type="submit"
            style={{
              height: 40,
              border: "none",
              borderRadius: 12,
              padding: "0 16px",
              background: "#111827",
              color: "#ffffff",
              fontWeight: 600,
              cursor: "pointer"
            }}
          >
            筛选
          </button>

          <a
            href="/users"
            style={{
              height: 40,
              display: "inline-flex",
              alignItems: "center",
              justifyContent: "center",
              borderRadius: 12,
              padding: "0 16px",
              border: "1px solid #d1d5db",
              color: "#111827",
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            重置
          </a>
        </form>

        <div style={{ color: "#4b5563", fontSize: 14 }}>
          {isFiltered ? `筛选结果 ${filteredRows.length} / ${rows.length} 位用户` : `共 ${rows.length} 位用户`}
        </div>
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
        <UserTable rows={filteredRows} />
      </div>
    </AdminShell>
  );
}
