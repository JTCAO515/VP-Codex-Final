"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const NAV_ITEMS = [
  { href: "/", label: "Chat" },
  { href: "/trips", label: "Trips" },
  { href: "/explore", label: "Explore" },
  { href: "/tools", label: "Tools" },
  { href: "/account", label: "Account" },
];

export function TopNav() {
  const pathname = usePathname();

  return (
    <header className="flex items-center justify-between border-b border-ink-umber/15 bg-ink-cream px-6 py-3">
      <div className="flex items-baseline gap-2">
        <span className="font-display text-2xl text-ink-cinnabar">VisePanda</span>
        <span className="text-sm text-ink-umber/60">AI China Travel Butler</span>
      </div>
      <nav className="flex gap-6">
        {NAV_ITEMS.map((item) => {
          const isActive = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`border-b-2 pb-1 text-sm font-medium ${
                isActive ? "border-ink-cinnabar text-ink-cinnabar" : "border-transparent text-ink-umber/70"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
    </header>
  );
}
