"use client";

import { CommunityHotSpots } from "@/components/community/CommunityHotSpots";
import { memberTiers } from "@/lib/community/membership";
import { useTranslation } from "@/lib/i18n/I18nContext";

export function CommunityBoard() {
  const { t } = useTranslation();

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

      <div className="community-board__body">
        <CommunityHotSpots />
      </div>
    </section>
  );
}
