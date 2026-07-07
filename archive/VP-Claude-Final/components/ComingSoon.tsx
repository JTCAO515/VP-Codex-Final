export function ComingSoon({ title }: { title: string }) {
  return (
    <div className="flex h-full items-center justify-center px-6 text-center">
      <div>
        <p className="font-display text-3xl text-ink-umber">{title}</p>
        <p className="mt-2 text-sm text-ink-umber/70">This part of VisePanda is coming in a later phase.</p>
      </div>
    </div>
  );
}
