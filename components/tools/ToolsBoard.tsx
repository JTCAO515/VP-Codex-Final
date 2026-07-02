"use client";

import { useEffect, useRef, useState } from "react";
import {
  AlertTriangle,
  CreditCard,
  DollarSign,
  FileCheck,
  Tv,
  Train,
} from "lucide-react";
import { getToolsProvider } from "@/lib/tools";
import type { ToolCategory } from "@/lib/tools";
import { useTranslation } from "@/lib/i18n/I18nContext";
import { ToolWidget } from "@/components/tools/widgets/ToolWidget";

type ToolMeta = { Icon: React.ElementType; accent: string; bg: string; badge: string };

const TOOL_META: Record<string, ToolMeta> = {
  "visa-and-entry":  { Icon: FileCheck,     accent: "#a33a2d", bg: "rgba(163,58,45,0.07)",   badge: "Required" },
  "payment-setup":   { Icon: CreditCard,    accent: "#b68634", bg: "rgba(182,134,52,0.07)",  badge: "Pre-trip" },
  "currency":        { Icon: DollarSign,    accent: "#667b5c", bg: "rgba(102,123,92,0.07)",  badge: "Live" },
  "metro":           { Icon: Train,         accent: "#4a6080", bg: "rgba(74,96,128,0.07)",   badge: "Transit" },
  "esim-vpn":        { Icon: Tv,            accent: "#7a5c8a", bg: "rgba(122,92,138,0.07)",  badge: "Connectivity" },
  "emergency":       { Icon: AlertTriangle, accent: "#9d2f24", bg: "rgba(157,47,36,0.07)",   badge: "Emergency" },
};

const FALLBACK_META: ToolMeta = { Icon: FileCheck, accent: "#6f5b49", bg: "rgba(111,91,73,0.07)", badge: "" };

function writeCategoryParam(id: string | null) {
  if (typeof window === "undefined") return;
  const u = new URL(window.location.href);
  id ? u.searchParams.set("category", id) : u.searchParams.delete("category");
  window.history.replaceState(null, "", `${u.pathname}${u.search}${u.hash}`);
}

export function ToolsBoard() {
  const [categories, setCategories] = useState<ToolCategory[]>([]);
  const [active, setActive] = useState<ToolCategory | null>(null);
  const modalRef = useRef<HTMLDivElement>(null);
  const { t } = useTranslation();

  const BADGE_LABELS: Record<string, string> = {
    "visa-and-entry": t.tools.badgeRequired,
    "payment-setup":  t.tools.badgePreTrip,
    "currency":       t.tools.badgeLive,
    "metro":          t.tools.badgeTransit,
    "esim-vpn":       t.tools.badgeConnectivity,
    "emergency":      t.tools.badgeEmergency,
  };

  useEffect(() => {
    getToolsProvider()
      .listCategories()
      .then((loaded) => {
        const req = typeof window !== "undefined"
          ? new URLSearchParams(window.location.search).get("category")
          : null;
        setCategories(loaded);
        const match = loaded.find((c) => c.id === req);
        if (match) setActive(match);
      });
  }, []);

  useEffect(() => {
    if (!active) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") close(); };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [active]);

  function open(cat: ToolCategory) {
    setActive(cat);
    writeCategoryParam(cat.id);
  }

  function close() {
    setActive(null);
    writeCategoryParam(null);
  }

  return (
    <section className="tools-board" aria-labelledby="tools-title">
      <header className="tools-board__header">
        <p className="section-kicker">{t.tools.kicker}</p>
        <h1 id="tools-title">{t.tools.heading}</h1>
        <p>{t.tools.subtitle}</p>
      </header>

      <div className="tools-grid" role="list" aria-label="Tool categories">
        {categories.map((cat) => {
          const { Icon, accent, bg } = TOOL_META[cat.id] ?? FALLBACK_META;
          const badge = BADGE_LABELS[cat.id] ?? (TOOL_META[cat.id] ?? FALLBACK_META).badge;
          return (
            <button
              aria-haspopup="dialog"
              aria-label={cat.name}
              className="tool-card"
              data-category-id={cat.id}
              key={cat.id}
              onClick={() => open(cat)}
              style={{ "--tool-accent": accent, "--tool-bg": bg } as React.CSSProperties}
              type="button"
            >
              <div className="tool-card__top">
                <span className="tool-card__icon-wrap">
                  <Icon color={accent} size={22} strokeWidth={1.6} />
                </span>
                {badge && <span className="tool-card__badge">{badge}</span>}
              </div>
              <strong className="tool-card__name">{cat.name}</strong>
              <p className="tool-card__desc">{cat.summary}</p>
              <span className="tool-card__cta">{t.tools.openChecklist}</span>
            </button>
          );
        })}
      </div>

      {active && (
        <div
          aria-labelledby="tool-modal-heading"
          aria-modal="true"
          className="tool-modal-overlay"
          onClick={close}
          role="dialog"
        >
          <div
            className="tool-modal"
            onClick={(e) => e.stopPropagation()}
            ref={modalRef}
          >
            {/* Modal header */}
            <div className="tool-modal__head">
              <span className="tool-modal__icon-wrap" style={{ background: (TOOL_META[active.id] ?? FALLBACK_META).bg }}>
                {(() => {
                  const { Icon, accent } = TOOL_META[active.id] ?? FALLBACK_META;
                  return <Icon color={accent} size={20} strokeWidth={1.6} />;
                })()}
              </span>
              <h2 className="tool-modal__title" id="tool-modal-heading">{active.name}</h2>
              <button aria-label={t.tools.close} className="tool-modal__close" onClick={close} type="button">✕</button>
            </div>

            <p className="tool-modal__summary">{active.summary}</p>

            <ToolWidget category={active} />

            {/* Quick tips */}
            {active.tips.length > 0 && (
              <ul className="tool-modal__tips">
                {active.tips.map((tip) => (
                  <li key={tip}>{tip}</li>
                ))}
              </ul>
            )}

            {/* Sections */}
            {active.sections.length > 0 && (
              <div className="tool-modal__sections">
                {active.sections.map((section) => (
                  <section key={section.title}>
                    <h3>{section.title}</h3>
                    <ul>
                      {section.items.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  </section>
                ))}
              </div>
            )}

            {/* Offline pocket notes */}
            {active.offlineTips.length > 0 && (
              <div className="tool-modal__offline">
                <h3>{t.tools.offlineHeading}</h3>
                <ul>
                  {active.offlineTips.map((tip) => (
                    <li key={tip}>{tip}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
}
