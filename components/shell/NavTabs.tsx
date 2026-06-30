import Link from "next/link";
import { Compass, Globe, Languages, Luggage, MessageCircle, Wrench } from "lucide-react";
import type { LucideIcon } from "lucide-react";

export type AppTab = "chat" | "trips" | "explore" | "tools" | "translate" | "community";

const tabs: Array<{ key: AppTab; label: string; href: string; icon: LucideIcon }> = [
  { key: "chat", label: "Chat", href: "/chat", icon: MessageCircle },
  { key: "trips", label: "Trips", href: "/trips", icon: Luggage },
  { key: "explore", label: "Explore", href: "/explore", icon: Compass },
  { key: "tools", label: "Tools", href: "/tools", icon: Wrench },
  { key: "translate", label: "Translate", href: "/translate", icon: Languages },
  { key: "community", label: "Community", href: "/community", icon: Globe },
];

export function NavTabs({ activeTab }: { activeTab: AppTab }) {
  return (
    <nav className="nav-tabs" aria-label="VisePanda sections">
      {tabs.map((tab) => {
        const Icon = tab.icon;

        return (
          <Link
            aria-current={activeTab === tab.key ? "page" : undefined}
            className="nav-tabs__link"
            data-active={activeTab === tab.key ? "true" : "false"}
            href={tab.href}
            key={tab.key}
          >
            <span aria-hidden="true">
              <Icon size={19} strokeWidth={2.2} />
            </span>
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
