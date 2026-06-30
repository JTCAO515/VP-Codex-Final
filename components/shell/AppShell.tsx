import type { ReactNode } from "react";
import { AccountMenu } from "@/components/account/AccountMenu";
import { NavTabs, type AppTab } from "@/components/shell/NavTabs";
import { LanguageSwitcher } from "@/components/shell/LanguageSwitcher";

interface AppShellProps {
  activeTab: AppTab;
  children: ReactNode;
}

export function AppShell({ activeTab, children }: AppShellProps) {
  return (
    <div className="app-shell">
      <header className="app-header">
        <a className="brand-mark" href="/chat" aria-label="VisePanda home">
          <img className="brand-mark__logo" src="/visepanda-logo-icon.jpg" alt="" aria-hidden="true" />
          <span>
            <strong>VisePanda</strong>
            <small>AI China Travel Butler</small>
          </span>
        </a>
        <div className="app-header__nav">
          <NavTabs activeTab={activeTab} />
          <LanguageSwitcher />
          <AccountMenu />
        </div>
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}
