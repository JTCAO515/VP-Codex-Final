import { ButlerWorkspace } from "@/components/chat/ButlerWorkspace";
import { AppShell } from "@/components/shell/AppShell";

export default function ChatPage() {
  return (
    <AppShell activeTab="chat">
      <ButlerWorkspace />
    </AppShell>
  );
}
