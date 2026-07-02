import { describe, expect, it } from "vitest";
import { getToolsProvider } from "@/lib/tools";

describe("static tools provider", () => {
  it("lists the six non-translate travel tool categories with non-empty details", async () => {
    const provider = getToolsProvider();
    const categories = await provider.listCategories();

    expect(categories).toHaveLength(6);
    expect(categories.map((category) => category.id)).toEqual([
      "visa-and-entry",
      "payment-setup",
      "currency",
      "metro",
      "esim-vpn",
      "emergency",
    ]);
    for (const category of categories) {
      expect(category.tips.length).toBeGreaterThan(0);
      expect(category.sections.length).toBeGreaterThan(0);
      expect(category.offlineTips.length).toBeGreaterThan(0);
      expect(category.apiPriority).toMatch(/later|next|not planned yet/i);
    }
    expect(categories.find((category) => category.id === "currency")?.interactive?.type).toBe("currency-converter");
    expect(categories.find((category) => category.id === "visa-and-entry")?.interactive?.type).toBe("visa-checker");
    expect(categories.find((category) => category.id === "payment-setup")?.interactive?.type).toBe("payment-wizard");
    expect(categories.find((category) => category.id === "metro")?.interactive).toBeUndefined();
  });
});
