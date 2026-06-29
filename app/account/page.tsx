import { AccountPanel } from "@/components/account/AccountPanel";
import { AppShell } from "@/components/shell/AppShell";

export default function AccountPage() {
  return (
    <AppShell activeTab="account">
      <AccountPanel />
    </AppShell>
  );
}
