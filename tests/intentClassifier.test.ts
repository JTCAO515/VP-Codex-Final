import { describe, expect, it } from "vitest";
import { classifyIntent } from "@/lib/ai/intentClassifier";

describe("classifyIntent", () => {
  it("classifies a create-trip request", () => {
    expect(classifyIntent("Plan me a 5 day trip in China")).toBe("create_trip");
    expect(classifyIntent("I want to travel to Beijing and Shanghai")).toBe("create_trip");
  });

  it("classifies an adjust-trip request", () => {
    expect(classifyIntent("Make the Beijing day calmer")).toBe("adjust_trip");
    expect(classifyIntent("This is too packed, lighten it")).toBe("adjust_trip");
  });

  it("classifies factual questions", () => {
    expect(classifyIntent("Do I need a visa?")).toBe("ask_factual");
    expect(classifyIntent("How do I pay with Alipay?")).toBe("ask_factual");
  });

  it("classifies recommendations, concerns, logistics, and preferences", () => {
    expect(classifyIntent("What's the best hotpot in Chengdu?")).toBe("ask_recommendation");
    expect(classifyIntent("Is it safe to travel alone?")).toBe("concern");
    expect(classifyIntent("How do I get from Xi'an to Chengdu?")).toBe("logistics");
    expect(classifyIntent("I'm a vegetarian and on a student budget")).toBe("preference_signal");
  });

  it("returns unclear for empty or ambiguous input", () => {
    expect(classifyIntent("")).toBe("unclear");
    expect(classifyIntent("hmm ok")).toBe("unclear");
  });
});
