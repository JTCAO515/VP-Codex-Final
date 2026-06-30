import { CommunityBoard } from "@/components/community/CommunityBoard";
import { AppShell } from "@/components/shell/AppShell";

export default function CommunityPage() {
  return (
    <AppShell activeTab="community">
      <CommunityBoard />
    </AppShell>
  );
}
