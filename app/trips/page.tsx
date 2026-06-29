import { PlaceholderPage } from "@/components/placeholders/PlaceholderPage";
import { AppShell } from "@/components/shell/AppShell";

export default function TripsPage() {
  return (
    <AppShell activeTab="trips">
      <PlaceholderPage
        eyebrow="Trips"
        title="Saved trips are coming next."
        description="Future trips, canvas snapshots, and AI planning history will appear here."
        items={["Saved canvases", "Trip history", "Continue planning", "Shareable drafts"]}
      />
    </AppShell>
  );
}
