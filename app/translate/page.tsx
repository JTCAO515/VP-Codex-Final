import { TranslatorPage } from "@/components/translate/TranslatorPage";
import { AppShell } from "@/components/shell/AppShell";

export default function TranslatePage() {
  return (
    <AppShell activeTab="translate">
      <TranslatorPage />
    </AppShell>
  );
}
