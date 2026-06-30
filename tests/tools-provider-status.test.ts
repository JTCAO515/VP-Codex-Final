import { describe, expect, it } from "vitest";
import { getToolsProvider } from "@/lib/tools";

describe("Tools provider status", () => {
  it("documents static mode and the first live data candidates", async () => {
    const status = await getToolsProvider().getProviderStatus();

    expect(status.mode).toBe("static");
    expect(status.label).toMatch(/static travel tools provider/i);
    expect(status.candidates).toEqual(expect.arrayContaining(["Exchange-rate API", "Visa rules API"]));
    expect(status.candidates).not.toContain("Machine translation API");
    expect(status.nextIntegration).toMatch(/exchange-rate/i);
  });
});
