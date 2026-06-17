import { AdminShell } from "../../components/admin-shell";
import { MetricCard } from "../../components/metric-card";
import { summarizeUsers } from "../../lib/dashboard";

export default function DashboardPage() {
  const summary = summarizeUsers([]);

  return (
    <AdminShell title="Dashboard" description="用户认证与后台运营概览。">
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
          gap: 16
        }}
      >
        <MetricCard label="Total Users" value={summary.total} />
        <MetricCard label="Active" value={summary.active} />
        <MetricCard label="Pending" value={summary.pending} />
        <MetricCard label="Disabled" value={summary.disabled} />
      </div>
    </AdminShell>
  );
}
