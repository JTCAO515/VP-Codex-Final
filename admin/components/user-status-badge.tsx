import type { UserStatus } from "../lib/types";

const paletteByStatus: Record<UserStatus, { background: string; color: string; label: string }> = {
  active: {
    background: "#dcfce7",
    color: "#166534",
    label: "激活"
  },
  disabled: {
    background: "#fee2e2",
    color: "#991b1b",
    label: "已禁用"
  },
  pending: {
    background: "#fef3c7",
    color: "#92400e",
    label: "待激活"
  }
};

export function UserStatusBadge({ status }: { status: UserStatus }) {
  const palette = paletteByStatus[status];

  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        borderRadius: 999,
        padding: "4px 10px",
        background: palette.background,
        color: palette.color,
        fontSize: 12,
        fontWeight: 600
      }}
    >
      {palette.label}
    </span>
  );
}
