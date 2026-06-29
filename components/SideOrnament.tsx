export function SideOrnament() {
  return (
    <div aria-hidden className="pointer-events-none fixed inset-y-0 left-0 -z-10 hidden w-40 md:block">
      <svg width="100%" height="100%" viewBox="0 0 160 900" preserveAspectRatio="xMinYMid slice">
        <rect width="160" height="900" fill="#f5ecdd" />
        <path d="M-20,820 Q30,700 70,760 T160,700 L160,900 L-20,900 Z" fill="#9c7d54" opacity="0.5" />
        <path d="M-20,860 Q40,780 90,830 T160,800 L160,900 L-20,900 Z" fill="#6e5634" opacity="0.4" />
        <circle cx="50" cy="200" r="3" fill="#6e5634" opacity="0.5" />
        <circle cx="80" cy="240" r="2.5" fill="#6e5634" opacity="0.4" />
        <defs>
          <linearGradient id="ornament-fade" x1="0" x2="1" y1="0" y2="0">
            <stop offset="0%" stopColor="#f5ecdd" stopOpacity="0" />
            <stop offset="100%" stopColor="#f5ecdd" stopOpacity="1" />
          </linearGradient>
        </defs>
        <rect width="160" height="900" fill="url(#ornament-fade)" />
      </svg>
    </div>
  );
}
