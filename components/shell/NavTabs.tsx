import Link from "next/link";

export type AppTab = "chat" | "trips" | "explore" | "tools" | "account";

const tabs: Array<{ key: AppTab; label: string; href: string; icon: string }> = [
  { key: "chat", label: "Chat", href: "/chat", icon: "C" },
  { key: "trips", label: "Trips", href: "/trips", icon: "T" },
  { key: "explore", label: "Explore", href: "/explore", icon: "E" },
  { key: "tools", label: "Tools", href: "/tools", icon: "X" },
  { key: "account", label: "Account", href: "/account", icon: "A" },
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
          <span aria-hidden="true">{tab.icon}</span>
          {tab.label}
        </Link>
      ))}
    </nav>
  );
}
