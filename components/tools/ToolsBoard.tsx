"use client";

import { useEffect, useState } from "react";
import { getToolsProvider } from "@/lib/tools";
import type { ToolCategory } from "@/lib/tools";

export function ToolsBoard() {
  const [categories, setCategories] = useState<ToolCategory[]>([]);
  const [activeCategoryId, setActiveCategoryId] = useState<string | null>(null);

  useEffect(() => {
    const provider = getToolsProvider();
    provider.listCategories().then((loaded) => {
      setCategories(loaded);
      setActiveCategoryId((current) => current ?? loaded[0]?.id ?? null);
    });
  }, []);

  const activeCategory = categories.find((category) => category.id === activeCategoryId) ?? null;

  return (
    <section className="tools-board" aria-labelledby="tools-title">
      <header className="tools-board__header">
        <p className="section-kicker">Tools</p>
        <h1 id="tools-title">On-the-ground travel tools</h1>
        <p>Practical checklists for visa, payment, language, currency, transit, connectivity, and emergencies.</p>
      </header>

      <div className="tools-board__body">
        <ul className="tools-category-list" aria-label="Tool categories">
          {categories.map((category) => (
            <li key={category.id}>
              <button
                aria-pressed={category.id === activeCategoryId}
                data-active={category.id === activeCategoryId ? "true" : "false"}
                onClick={() => setActiveCategoryId(category.id)}
                type="button"
              >
                {category.name}
              </button>
            </li>
          ))}
        </ul>

        {activeCategory && (
          <article className="tools-category-detail" aria-labelledby="tools-category-title">
            <h2 id="tools-category-title">{activeCategory.name}</h2>
            <p>{activeCategory.summary}</p>
            <ul aria-label={`${activeCategory.name} tips`}>
              {activeCategory.tips.map((tip) => (
                <li key={tip}>{tip}</li>
              ))}
            </ul>
          </article>
        )}
      </div>
    </section>
  );
}
