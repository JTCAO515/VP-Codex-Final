"use client";

import { useState } from "react";
import { CommunityFeed } from "@/components/community/CommunityFeed";
import { CommunityHotSpots } from "@/components/community/CommunityHotSpots";
import { CommunityPhotos } from "@/components/community/CommunityPhotos";
import { memberTiers } from "@/lib/community/membership";
import { useTranslation } from "@/lib/i18n/I18nContext";

type CommunityTab = "feed" | "hotspots" | "photos";

export function CommunityBoard() {
  const [activeTab, setActiveTab] = useState<CommunityTab>("feed");
  const { t } = useTranslation();

  const TABS: Array<{ key: CommunityTab; label: string }> = [
    { key: "feed",     label: t.community.tabFeed },
    { key: "hotspots", label: t.community.tabHotSpots },
    { key: "photos",   label: t.community.tabPhotos },
  ];

  return (
    <section className="community-board" aria-labelledby="community-title">
      <header className="community-board__header">
        <p className="section-kicker">{t.community.kicker}</p>
        <h1 id="community-title">{t.community.heading}</h1>
        <p>{t.community.subtitle}</p>
      </header>

      <div className="community-membership-strip" aria-label="VisePanda membership levels">
        {memberTiers.map((tier) => (
          <article className="community-membership-strip__tier" key={tier.id}>
            <strong>{tier.name}</strong>
            <span>{tier.requirement}</span>
          </article>
        ))}
      </div>

      <div className="community-tabs" role="tablist" aria-label="Community sections">
        {TABS.map((tab) => (
          <button
            aria-selected={tab.key === activeTab}
            className={`community-tabs__btn${tab.key === activeTab ? " active" : ""}`}
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            role="tab"
            type="button"
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="community-board__body">
        {activeTab === "feed" && <CommunityFeed />}
        {activeTab === "hotspots" && <CommunityHotSpots />}
        {activeTab === "photos" && <CommunityPhotos />}
      </div>
    </section>
  );
}
