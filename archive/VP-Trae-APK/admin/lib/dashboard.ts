export function summarizeUsers(items: Array<{ status: "active" | "pending" | "disabled" }>) {
  return {
    total: items.length,
    active: items.filter((item) => item.status === "active").length,
    pending: items.filter((item) => item.status === "pending").length,
    disabled: items.filter((item) => item.status === "disabled").length
  };
}
