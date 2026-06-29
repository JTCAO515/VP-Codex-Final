import { PlaceholderPage } from "@/components/placeholders/PlaceholderPage";
import { AppShell } from "@/components/shell/AppShell";

export default function AccountPage() {
  return (
    <AppShell activeTab="account">
      <PlaceholderPage
        eyebrow="Account"
        title="Sign in and sync are coming later."
        description="Guest mode is available for the first-stage skeleton. Supabase-backed sync will be added after the butler workspace is solid."
        items={["Guest mode", "Future sign in", "Trip sync", "Chat history", "Saved preferences"]}
      />
    </AppShell>
  );
}
