import { describe, expect, it } from "vitest";
import { getToolsProvider } from "@/lib/tools";

describe("static tools provider", () => {
  it("lists all seven travel tool categories with non-empty tips", async () => {
    const provider = getToolsProvider();
    const categories = await provider.listCategories();

    expect(categories).toHaveLength(7);
    expect(categories.map((category) => category.id)).toEqual([
      "visa-and-entry",
      "payment-setup",
      "translate",
      "currency",
      "metro",
      "esim-vpn",
      "emergency",
    ]);
    for (const category of categories) {
      expect(category.tips.length).toBeGreaterThan(0);
    }
  });
});
