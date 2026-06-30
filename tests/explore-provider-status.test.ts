import { describe, expect, it } from "vitest";
import { getExploreProvider } from "@/lib/explore";

describe("Explore provider status", () => {
  it("documents static mode and the candidate third-party providers", async () => {
    const status = await getExploreProvider().getProviderStatus();

    expect(status.mode).toBe("static");
    expect(status.label).toMatch(/static curated provider/i);
    expect(status.candidates).toEqual(expect.arrayContaining(["Amap", "Trip.com", "Meituan", "Tripadvisor"]));
    expect(status.nextIntegration).toMatch(/POI/i);
  });
});
