import Link from "next/link";

interface PlaceholderPageProps {
  eyebrow: string;
  title: string;
  description: string;
  items: string[];
}

export function PlaceholderPage({ eyebrow, title, description, items }: PlaceholderPageProps) {
  return (
    <section className="placeholder-page" aria-labelledby="placeholder-title">
      <div className="placeholder-page__copy">
        <p className="section-kicker">{eyebrow}</p>
        <h1 id="placeholder-title">{title}</h1>
        <p>{description}</p>
        <Link className="primary-link" href="/chat">
          Return to Chat
        </Link>
      </div>
      <ul className="placeholder-list" aria-label={`${eyebrow} future scope`}>
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </section>
  );
}
