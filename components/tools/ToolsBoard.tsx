"use client";

import { useEffect, useState } from "react";
import { getToolsProvider } from "@/lib/tools";
import type { ToolCategory } from "@/lib/tools";

function readCategoryParam() {
  if (typeof window === "undefined") {
    return null;
  }

  return new URLSearchParams(window.location.search).get("category");
}

function writeCategoryParam(categoryId: string) {
  if (typeof window === "undefined") {
    return;
  }

  const nextUrl = new URL(window.location.href);
  nextUrl.searchParams.set("category", categoryId);
  window.history.replaceState(null, "", `${nextUrl.pathname}${nextUrl.search}${nextUrl.hash}`);
}

export function ToolsBoard() {
  const [categories, setCategories] = useState<ToolCategory[]>([]);
  const [activeCategoryId, setActiveCategoryId] = useState<string | null>(null);

  useEffect(() => {
    const provider = getToolsProvider();
    provider.listCategories().then((loaded) => {
      const requestedCategoryId = readCategoryParam();
      const initialCategoryId =
        loaded.find((category) => category.id === requestedCategoryId)?.id ?? loaded[0]?.id ?? null;

      setCategories(loaded);
      setActiveCategoryId((current) => current ?? initialCategoryId);
    });
  }, []);

  const activeCategory = categories.find((category) => category.id === activeCategoryId) ?? null;

  function handleCategorySelect(categoryId: string) {
    setActiveCategoryId(categoryId);
    writeCategoryParam(categoryId);
  }

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
                data-category-id={category.id}
                onClick={() => handleCategorySelect(category.id)}
                type="button"
              >
                {category.name}
              </button>
            </li>
          ))}
        </ul>

        {activeCategory && (
          <article
            className="tools-category-detail"
            aria-labelledby="tools-category-title"
            data-category-id={activeCategory.id}
            id={`tool-${activeCategory.id}`}
          >
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
