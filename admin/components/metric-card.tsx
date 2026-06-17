export function MetricCard({ label, value }: { label: string; value: number }) {
  return (
    <div
      style={{
        border: "1px solid #e5e7eb",
        borderRadius: 16,
        background: "#ffffff",
        padding: 20,
        boxShadow: "0 10px 30px rgba(15, 23, 42, 0.05)"
      }}
    >
      <div style={{ color: "#6b7280", fontSize: 14, marginBottom: 8 }}>{label}</div>
      <div style={{ fontSize: 30, fontWeight: 700, color: "#111827" }}>{value}</div>
    </div>
  );
}
