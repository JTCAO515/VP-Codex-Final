"use client";

import Link from "next/link";
import {
  Compass,
  Globe,
  Languages,
  Luggage,
  MessageCircle,
  Wrench,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { AccountMenu } from "@/components/account/AccountMenu";
import { LanguageSwitcher } from "@/components/shell/LanguageSwitcher";
import { useTranslation } from "@/lib/i18n/I18nContext";
import type { Translations } from "@/lib/i18n/translations/en";

type Feature = {
  key: string;
  href: string;
  Icon: LucideIcon;
  accent: string;
  bg: string;
  getTitle: (t: Translations) => string;
  desc: string;
};

const FEATURES: Feature[] = [
  {
    key: "chat",
    href: "/chat",
    Icon: MessageCircle,
    accent: "#a33a2d",
    bg: "rgba(163,58,45,0.07)",
    getTitle: (t) => t.nav.chat,
    desc: "AI Butler builds your full itinerary in real time as you chat.",
  },
  {
    key: "trips",
    href: "/trips",
    Icon: Luggage,
    accent: "#b68634",
    bg: "rgba(182,134,52,0.07)",
    getTitle: (t) => t.nav.trips,
    desc: "Save, revisit, and share your China trip canvases.",
  },
  {
    key: "explore",
    href: "/explore",
    Icon: Compass,
    accent: "#667b5c",
    bg: "rgba(102,123,92,0.07)",
    getTitle: (t) => t.nav.explore,
    desc: "Curated attractions, food spots, and stays across China.",
  },
  {
    key: "tools",
    href: "/tools",
    Icon: Wrench,
    accent: "#4a6080",
    bg: "rgba(74,96,128,0.07)",
    getTitle: (t) => t.nav.tools,
    desc: "Visa, payments, metro, eSIM, currency, and emergency info.",
  },
  {
    key: "translate",
    href: "/translate",
    Icon: Languages,
    accent: "#7a5c8a",
    bg: "rgba(122,92,138,0.07)",
    getTitle: (t) => t.nav.translate,
    desc: "Text, OCR scan, voice, and phrase book in one view.",
  },
  {
    key: "community",
    href: "/community",
    Icon: Globe,
    accent: "#5c7a6e",
    bg: "rgba(92,122,110,0.07)",
    getTitle: (t) => t.nav.community,
    desc: "Trip stories, hot spots, and photos from real travelers.",
  },
];

export function HomePage() {
  const { t } = useTranslation();

  return (
    <div className="home-page">
      <header className="home-header">
        <a className="brand-mark" href="/" aria-label="VisePanda home">
          <img
            className="brand-mark__logo"
            src="/visepanda-logo-icon.jpg"
            alt=""
            aria-hidden="true"
          />
          <span>
            <strong>VisePanda</strong>
            <small>AI China Travel Butler</small>
          </span>
        </a>
        <div className="home-header__actions">
          <LanguageSwitcher />
          <AccountMenu />
        </div>
      </header>

      <section className="home-hero" aria-labelledby="home-headline">
        <p className="home-hero__kicker">Plan · Explore · Travel</p>
        <h1 id="home-headline" className="home-hero__headline">
          Your AI China<br />Travel Butler
        </h1>
        <p className="home-hero__sub">
          Tell the AI what you want — it builds a live itinerary canvas.<br />
          Explore cities, use on-the-ground tools, join the traveler community.
        </p>
        <Link className="home-hero__cta" href="/chat">
          Start Planning →
        </Link>
      </section>

      <section className="home-features" aria-label="VisePanda features">
        {FEATURES.map(({ key, href, Icon, accent, bg, getTitle, desc }) => (
          <Link
            className="home-feature-card"
            href={href}
            key={key}
            style={{ "--feat-accent": accent, "--feat-bg": bg } as React.CSSProperties}
          >
            <span className="home-feature-card__icon">
              <Icon color={accent} size={22} strokeWidth={1.7} />
            </span>
            <strong className="home-feature-card__title">{getTitle(t)}</strong>
            <p className="home-feature-card__desc">{desc}</p>
          </Link>
        ))}
      </section>

      <footer className="home-footer">
        <span>© 2026 VisePanda · go2china.space</span>
      </footer>
    </div>
  );
}
