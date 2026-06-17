import Link from 'next/link';

export function AppHeader() {
  return (
    <header className="border-b bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link href="/" className="font-semibold text-zinc-900">
          VisePanda · Traveler Web
        </Link>
        <nav className="flex items-center gap-4 text-sm text-zinc-600">
          <Link className="hover:text-zinc-900" href="/destinations">
            目的地
          </Link>
          <Link className="hover:text-zinc-900" href="/tools">
            工具
          </Link>
          <Link className="hover:text-zinc-900" href="/ai/plan-trip">
            AI 规划
          </Link>
          <Link className="hover:text-zinc-900" href="/trips">
            我的行程
          </Link>
        </nav>
      </div>
    </header>
  );
}
