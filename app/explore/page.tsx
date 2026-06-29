import { PlaceholderPage } from "@/components/placeholders/PlaceholderPage";
import { AppShell } from "@/components/shell/AppShell";

export default function ExplorePage() {
  return (
    <AppShell activeTab="explore">
      <PlaceholderPage
        eyebrow="Explore"
        title="Explore is coming next."
        description="Cities, attractions, dining, hotels, and local experiences will connect here through verified provider layers."
        items={["Cities", "Attractions", "Food", "Hotels", "Local experiences"]}
      />
    </AppShell>
  );
}
