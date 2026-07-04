"use client";

import Link from "next/link";
import { Compass, Globe, Languages, Luggage, MessageCircle, Wrench } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useTranslation } from "@/lib/i18n/I18nContext";

export type AppTab = "chat" | "trips" | "explore" | "tools" | "translate" | "community";

type TabDef = { key: AppTab; href: string; icon: LucideIcon };

const TAB_DEFS: TabDef[] = [
  { key: "chat",      href: "/chat",      icon: MessageCircle },
  { key: "trips",     href: "/trips",     icon: Luggage },
  { key: "community", href: "/community", icon: Globe },
];

export function NavTabs({ activeTab }: { activeTab: AppTab }) {
  const { t } = useTranslation();

  return (
    <nav className="nav-tabs" aria-label="VisePanda sections">
      {TAB_DEFS.map((tab) => {
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
            {t.nav[tab.key]}
          </Link>
        );
      })}
    </nav>
  );
}
