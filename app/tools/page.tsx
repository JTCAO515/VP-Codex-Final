import { AppShell } from "@/components/shell/AppShell";
import { ToolsBoard } from "@/components/tools/ToolsBoard";

export default function ToolsPage() {
  return (
    <AppShell activeTab="tools">
      <ToolsBoard />
    </AppShell>
  );
}
