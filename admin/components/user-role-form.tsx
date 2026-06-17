"use client";

import { useMemo, useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import type { UserProfile, UserRole, UserStatus } from "../lib/types";

type UserRoleFormProps = {
  user: UserProfile;
};

const FIELD_STYLE = {
  display: "grid",
  gap: 8
} as const;

export function UserRoleForm({ user }: UserRoleFormProps) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [displayName, setDisplayName] = useState(user.display_name ?? "");
  const [avatarUrl, setAvatarUrl] = useState(user.avatar_url ?? "");
  const [role, setRole] = useState<UserRole>(user.role);
  const [status, setStatus] = useState<UserStatus>(user.status);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const hasChanges = useMemo(() => {
    return (
      displayName !== (user.display_name ?? "") ||
      avatarUrl !== (user.avatar_url ?? "") ||
      role !== user.role ||
      status !== user.status
    );
  }, [avatarUrl, displayName, role, status, user.avatar_url, user.display_name, user.role, user.status]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setMessage(null);

    const response = await fetch(`/api/users/${user.id}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        display_name: displayName,
        avatar_url: avatarUrl,
        role,
        status
      })
    });

    const payload = (await response.json()) as { error?: string };

    if (!response.ok) {
      setError(payload.error ?? "保存用户信息失败。");
      return;
    }

    setMessage("用户信息已更新。");
    startTransition(() => {
      router.refresh();
    });
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: "grid", gap: 16 }}>
      <div style={FIELD_STYLE}>
        <label htmlFor="display_name">显示名称</label>
        <input
          id="display_name"
          name="display_name"
          value={displayName}
          onChange={(event) => setDisplayName(event.target.value)}
          placeholder="例如 Alice"
          style={{ padding: "10px 12px", borderRadius: 10, border: "1px solid #d1d5db" }}
        />
      </div>

      <div style={FIELD_STYLE}>
        <label htmlFor="avatar_url">头像 URL</label>
        <input
          id="avatar_url"
          name="avatar_url"
          value={avatarUrl}
          onChange={(event) => setAvatarUrl(event.target.value)}
          placeholder="https://example.com/avatar.png"
          style={{ padding: "10px 12px", borderRadius: 10, border: "1px solid #d1d5db" }}
        />
      </div>

      <div style={{ display: "grid", gap: 16, gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))" }}>
        <div style={FIELD_STYLE}>
          <label htmlFor="role">角色</label>
          <select
            id="role"
            name="role"
            value={role}
            onChange={(event) => setRole(event.target.value as UserRole)}
            style={{ padding: "10px 12px", borderRadius: 10, border: "1px solid #d1d5db" }}
          >
            <option value="user">普通用户</option>
            <option value="admin">管理员</option>
          </select>
        </div>

        <div style={FIELD_STYLE}>
          <label htmlFor="status">状态</label>
          <select
            id="status"
            name="status"
            value={status}
            onChange={(event) => setStatus(event.target.value as UserStatus)}
            style={{ padding: "10px 12px", borderRadius: 10, border: "1px solid #d1d5db" }}
          >
            <option value="pending">待激活</option>
            <option value="active">正常</option>
            <option value="disabled">禁用</option>
          </select>
        </div>
      </div>

      {error ? <div style={{ color: "#b91c1c", fontSize: 14 }}>{error}</div> : null}
      {message ? <div style={{ color: "#166534", fontSize: 14 }}>{message}</div> : null}

      <button
        type="submit"
        disabled={!hasChanges || isPending}
        style={{
          justifySelf: "start",
          padding: "10px 16px",
          borderRadius: 10,
          border: "none",
          background: hasChanges ? "#111827" : "#9ca3af",
          color: "#ffffff",
          cursor: hasChanges ? "pointer" : "not-allowed"
        }}
      >
        {isPending ? "刷新中..." : "保存更改"}
      </button>
    </form>
  );
}
