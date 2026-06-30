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

function writeCategoryParam(categoryId: string | null) {
  if (typeof window === "undefined") {
    return;
  }

  const nextUrl = new URL(window.location.href);
  if (categoryId) {
    nextUrl.searchParams.set("category", categoryId);
  } else {
    nextUrl.searchParams.delete("category");
  }
  window.history.replaceState(null, "", `${nextUrl.pathname}${nextUrl.search}${nextUrl.hash}`);
}

function toDomId(value: string) {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
}

export function ToolsBoard() {
  const [categories, setCategories] = useState<ToolCategory[]>([]);
  const [activeCategoryId, setActiveCategoryId] = useState<string | null>(null);

  useEffect(() => {
    const provider = getToolsProvider();
    provider.listCategories().then((loaded) => {
      const requestedCategoryId = readCategoryParam();
      const initialCategoryId = loaded.find((category) => category.id === requestedCategoryId)?.id ?? null;

      setCategories(loaded);
      setActiveCategoryId((current) => current ?? initialCategoryId);
    });
  }, []);

  const activeCategory = categories.find((category) => category.id === activeCategoryId) ?? null;

  function handleCategorySelect(categoryId: string) {
    setActiveCategoryId((current) => {
      const next = current === categoryId ? null : categoryId;
      writeCategoryParam(next);
      return next;
    });
  }

  return (
    <section className="tools-board" aria-labelledby="tools-title">
      <header className="tools-board__header">
        <p className="section-kicker">Tools</p>
        <h1 id="tools-title">On-the-ground travel tools</h1>
        <p>Practical checklists for visa, payment, currency, transit, connectivity, and emergencies.</p>
      </header>

      <div className="tools-board__body">
        <ul className="tools-category-list tools-category-list--cards" aria-label="Tool categories">
          {categories.map((category) => (
            <li key={category.id}>
              <button
                aria-expanded={category.id === activeCategoryId}
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

        {activeCategory ? (
          <article
            className="tools-category-detail tools-category-drawer"
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
            <div className="tools-category-sections">
              {activeCategory.sections.map((section) => {
                const sectionId = `tool-section-${activeCategory.id}-${toDomId(section.title)}`;

                return (
                  <section key={section.title} aria-labelledby={sectionId}>
                    <h3 id={sectionId}>{section.title}</h3>
                    <ul>
                      {section.items.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  </section>
                );
              })}
            </div>
            <section className="tools-offline-notes" aria-labelledby={`offline-notes-${activeCategory.id}`}>
              <h3 id={`offline-notes-${activeCategory.id}`}>Offline pocket notes</h3>
              <ul>
                {activeCategory.offlineTips.map((tip) => (
                  <li key={tip}>{tip}</li>
                ))}
              </ul>
            </section>
          </article>
        ) : (
          <p className="tools-drawer-empty">Select a tool card to open its checklist.</p>
        )}
      </div>
    </section>
  );
}
