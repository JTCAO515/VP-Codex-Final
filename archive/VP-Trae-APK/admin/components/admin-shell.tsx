import Link from "next/link";
import type { ReactNode } from "react";

type AdminShellProps = {
  title: string;
  description?: string;
  children: ReactNode;
};

const shellStyle = {
  minHeight: "100vh",
  background: "#f5f7fb",
  color: "#111827"
} as const;

const containerStyle = {
  maxWidth: 1120,
  margin: "0 auto",
  padding: "32px 24px 48px"
} as const;

const navigationStyle = {
  display: "flex",
  gap: 16,
  marginBottom: 32,
  fontSize: 14
} as const;

const cardStyle = {
  background: "#ffffff",
  border: "1px solid #e5e7eb",
  borderRadius: 16,
  padding: 24,
  boxShadow: "0 12px 32px rgba(15, 23, 42, 0.06)"
} as const;

export function AdminShell({ title, description, children }: AdminShellProps) {
  return (
    <main style={shellStyle}>
      <div style={containerStyle}>
        <nav aria-label="Admin navigation" style={navigationStyle}>
          <Link href="/">概览</Link>
          <Link href="/users">用户列表</Link>
          <Link href="/login">管理员登录</Link>
        </nav>
        <section style={cardStyle}>
          <header style={{ marginBottom: 24 }}>
            <h1 style={{ margin: 0, fontSize: 32 }}>{title}</h1>
            {description ? (
              <p style={{ margin: "12px 0 0", color: "#4b5563", lineHeight: 1.6 }}>{description}</p>
            ) : null}
          </header>
          {children}
        </section>
      </div>
    </main>
  );
}
