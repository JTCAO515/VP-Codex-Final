import Link from "next/link";

export type AppTab = "chat" | "trips" | "explore" | "tools" | "account";

const tabs: Array<{ key: AppTab; label: string; href: string }> = [
  { key: "chat", label: "Chat", href: "/chat" },
  { key: "trips", label: "Trips", href: "/trips" },
  { key: "explore", label: "Explore", href: "/explore" },
  { key: "tools", label: "Tools", href: "/tools" },
  { key: "account", label: "Account", href: "/account" },
];

export function NavTabs({ activeTab }: { activeTab: AppTab }) {
  return (
    <nav className="nav-tabs" aria-label="VisePanda sections">
      {tabs.map((tab) => (
        <Link
          aria-current={activeTab === tab.key ? "page" : undefined}
          className="nav-tabs__link"
          data-active={activeTab === tab.key ? "true" : "false"}
          href={tab.href}
          key={tab.key}
        >
          {tab.label}
        </Link>
      ))}
    </nav>
  );
}
