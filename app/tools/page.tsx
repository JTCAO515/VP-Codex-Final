import { PlaceholderPage } from "@/components/placeholders/PlaceholderPage";
import { AppShell } from "@/components/shell/AppShell";

export default function ToolsPage() {
  return (
    <AppShell activeTab="tools">
      <PlaceholderPage
        eyebrow="Tools"
        title="On-the-ground tools are coming next."
        description="Translation, payment, visa, currency, metro, eSIM, and emergency helpers will live here."
        items={["Translate", "Payment setup", "Visa and entry", "Currency", "Metro", "eSIM/VPN", "Emergency"]}
      />
    </AppShell>
  );
}
