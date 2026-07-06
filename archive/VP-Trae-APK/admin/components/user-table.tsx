import Link from "next/link";
import { UserStatusBadge } from "./user-status-badge";
import type { UserRole, UserStatus } from "../lib/types";

export type UserRow = {
  id: string;
  title: string;
  subtitle: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  lastLoginAt: string;
};

type UserTableProps = {
  rows: UserRow[];
};

const tableCellStyle = {
  padding: "14px 16px",
  borderBottom: "1px solid #e5e7eb",
  textAlign: "left"
} as const;

export function UserTable({ rows }: UserTableProps) {
  if (rows.length === 0) {
    return (
      <div
        style={{
          border: "1px dashed #cbd5e1",
          borderRadius: 16,
          padding: 24,
          background: "#f8fafc",
          color: "#475569"
        }}
      >
        当前还没有可展示的用户数据。
      </div>
    );
  }

  return (
    <div
      style={{
        overflowX: "auto",
        border: "1px solid #e5e7eb",
        borderRadius: 16,
        background: "#ffffff"
      }}
    >
      <table style={{ width: "100%", borderCollapse: "collapse", minWidth: 760 }}>
        <thead style={{ background: "#f8fafc" }}>
          <tr>
            <th style={tableCellStyle}>用户</th>
            <th style={tableCellStyle}>角色</th>
            <th style={tableCellStyle}>状态</th>
            <th style={tableCellStyle}>创建时间</th>
            <th style={tableCellStyle}>最近登录</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              <td style={tableCellStyle}>
                <div style={{ fontWeight: 600 }}>
                  <Link href={`/users/${row.id}`}>{row.title}</Link>
                </div>
                <div style={{ fontSize: 13, color: "#6b7280", marginTop: 4 }}>{row.subtitle}</div>
              </td>
              <td style={tableCellStyle}>{row.role === "admin" ? "管理员" : "普通用户"}</td>
              <td style={tableCellStyle}>
                <UserStatusBadge status={row.status} />
              </td>
              <td style={tableCellStyle}>{row.createdAt}</td>
              <td style={tableCellStyle}>{row.lastLoginAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
