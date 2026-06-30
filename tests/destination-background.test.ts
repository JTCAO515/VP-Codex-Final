import { describe, expect, it } from "vitest";
import { getDestinationScene } from "@/lib/visual/destinationBackground";

describe("getDestinationScene", () => {
  it("prioritizes Beijing and Shanghai destination scenes from the trip route", () => {
    expect(getDestinationScene(["Beijing", "Shanghai"]).id).toBe("beijing-imperial");
    expect(getDestinationScene(["Suzhou", "Shanghai"]).id).toBe("shanghai-jiangnan");
  });

  it("maps Jiangnan and mountain-city destinations to their visual scene families", () => {
    expect(getDestinationScene(["Hangzhou"]).id).toBe("jiangnan-lake");
    expect(getDestinationScene(["Chongqing"]).id).toBe("mountain-river");
  });

  it("falls back to the default ink landscape when no destination matches", () => {
    expect(getDestinationScene(["Kunming"]).id).toBe("default-ink");
  });
});
