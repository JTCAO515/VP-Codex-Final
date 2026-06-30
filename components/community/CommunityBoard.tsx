"use client";

import { useState } from "react";
import { CommunityFeed } from "@/components/community/CommunityFeed";
import { CommunityPhotos } from "@/components/community/CommunityPhotos";
import { CommunityHotSpots } from "@/components/community/CommunityHotSpots";

type CommunityTab = "feed" | "hotspots" | "photos";

const TABS: Array<{ key: CommunityTab; label: string }> = [
  { key: "feed", label: "动态 Feed" },
  { key: "hotspots", label: "热门 Hot Spots" },
  { key: "photos", label: "照片 Photos" },
];

export function CommunityBoard() {
  const [activeTab, setActiveTab] = useState<CommunityTab>("feed");

  return (
    <section className="community-board" aria-labelledby="community-title">
      <header className="community-board__header">
        <p className="section-kicker">社区 / Community</p>
        <h1 id="community-title">旅行者社区</h1>
        <p>分享行程、照片和旅行心得 · Share trips, photos, and travel tips with fellow travelers</p>
      </header>

      <div className="community-tabs" role="tablist" aria-label="社区板块">
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
