import { ExploreBoard } from "@/components/explore/ExploreBoard";
import { AppShell } from "@/components/shell/AppShell";

export default function ExplorePage() {
  return (
    <AppShell activeTab="explore">
      <ExploreBoard />
    </AppShell>
  );
}
