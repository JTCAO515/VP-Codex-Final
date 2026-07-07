import { createStaticToolsProvider } from "@/lib/tools/staticProvider";
import type { ButlerIntent } from "@/lib/ai/intentClassifier";
import type { CanvasPatch, InlineToolCard, TripState } from "@/lib/types/trip";
import type { ToolCategory } from "@/lib/tools/types";

interface FactualToolResponse {
  patch: CanvasPatch;
  suggestions: string[];
  categoryId: string;
}

interface CategoryRule {
  categoryId: string;
  patterns: RegExp[];
  headline: string;
  nextStep: string;
  watchOut?: string;
  tone?: InlineToolCard["tone"];
  suggestions: [string, string];
}

const CATEGORY_RULES: CategoryRule[] = [
  {
    categoryId: "visa-and-entry",
    patterns: [/\b(visa|passport|entry|customs|transit|144|240)\b/i],
    headline: "Check entry rules before you book",
    nextStep: "Open the visa and entry checklist",
    watchOut: "Visa and transit rules change often, so confirm the final answer with an official embassy or consulate source.",
    tone: "warning",
    suggestions: ["What documents should I prepare?", "Add this as a prep reminder"],
  },
  {
    categoryId: "payment-setup",
    patterns: [/\b(alipay|wechat pay|pay|payment|card|visa card|mastercard|amex|cash)\b/i],
    headline: "Set up mobile payment before arrival",
    nextStep: "Open the payment setup guide",
    watchOut: "Keep a small RMB cash backup because identity checks, card limits, or app verification can fail at inconvenient moments.",
    suggestions: ["Which payment app should I set up first?", "Add payment setup to my prep list"],
  },
  {
    categoryId: "currency",
    patterns: [/\b(currency|exchange|rate|rmb|yuan|cny|cash|atm|how much)\b/i],
    headline: "Use RMB with a simple backup plan",
    nextStep: "Open the currency tool",
    suggestions: ["How much cash should I carry?", "Explain common RMB amounts"],
  },
  {
    categoryId: "metro",
    patterns: [/\b(metro|subway|transit qr|train station|station|ticket machine)\b/i],
    headline: "Metro is easiest with a transit QR backup",
    nextStep: "Open the metro guide",
    suggestions: ["How do I buy metro tickets?", "Add metro notes to my trip"],
  },
  {
    categoryId: "esim-vpn",
    patterns: [/\b(esim|sim card|vpn|wifi|internet|data roaming|connectivity)\b/i],
    headline: "Connectivity should be ready before you land",
    nextStep: "Open the eSIM and VPN checklist",
    watchOut: "Test the setup before departure; trying to fix account access after landing is much harder.",
    suggestions: ["What should I download offline?", "Add connectivity prep to my trip"],
  },
  {
    categoryId: "emergency",
    patterns: [
      /\b(emergency|hospital|police|ambulance|embassy|consulate|lost (my )?passport|help)\b/i,
      /\b(robbed|stolen|theft|pickpocket|scammed|injured|hurt|urgent)\b/i,
    ],
    headline: "Keep emergency details offline",
    nextStep: "Open the emergency card",
    watchOut: "For urgent danger or medical emergencies, contact local emergency services immediately rather than waiting for AI guidance.",
    tone: "warning",
    suggestions: ["What numbers should I save?", "Prepare a Chinese help phrase"],
  },
];

function chooseRule(message: string, intent: ButlerIntent): CategoryRule | undefined {
  if (intent !== "ask_factual" && intent !== "concern") return undefined;
  return CATEGORY_RULES.find((rule) => rule.patterns.some((pattern) => pattern.test(message)));
}

function buildCard(category: ToolCategory, rule: CategoryRule, trip: TripState): InlineToolCard {
  const primarySectionItems = category.sections.find((section) => section.items.length > 0)?.items ?? [];
  const items = [...category.tips, ...primarySectionItems].slice(0, 4);
  const destination = trip.summary.destinations[0];
  const destinationHint = destination ? ` for your ${destination} plan` : "";

  return {
    id: `tool-${category.id}`,
    categoryId: category.id,
    title: category.name,
    summary: `${category.summary}${destinationHint}.`,
    items,
    nextAction: rule.nextStep,
    href: `/tools?category=${category.id}`,
    tone: rule.tone ?? "info",
    sourceLabel: "VisePanda Tools",
  };
}

export async function buildFactualToolResponse(input: {
  message: string;
  currentTrip: TripState;
  intent: ButlerIntent;
}): Promise<FactualToolResponse | undefined> {
  const rule = chooseRule(input.message, input.intent);
  if (!rule) return undefined;

  const categories = await createStaticToolsProvider().listCategories();
  const category = categories.find((candidate) => candidate.id === rule.categoryId);
  if (!category) return undefined;

  const card = buildCard(category, rule, input.currentTrip);
  const highlights = card.items.slice(0, 3);

  return {
    categoryId: category.id,
    suggestions: rule.suggestions,
    patch: {
      intent: "add_alerts",
      assistantMessage: `${rule.headline}. ${category.summary}`,
      assistantResponse: {
        headline: rule.headline,
        body: category.summary,
        highlights,
        watchOut: rule.watchOut,
        nextStep: rule.nextStep,
        toolCards: [card],
      },
      reason: `Answered with the ${category.name} tool card.`,
    },
  };
}
