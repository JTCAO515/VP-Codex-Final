import { createStaticToolsProvider } from "@/lib/tools/staticProvider";
import type { ToolCategory, ToolsProvider, ToolsProviderStatus } from "@/lib/tools/types";

interface ExchangeRateData {
  base: string;
  rates: Record<string, number>;
  updatedAt: string;
}

async function fetchLiveRates(): Promise<ExchangeRateData | null> {
  try {
    const res = await fetch("/api/exchange-rate");
    if (!res.ok) return null;
    const data = await res.json();
    return data.ok ? (data as ExchangeRateData) : null;
  } catch {
    return null;
  }
}

function injectLiveRates(category: ToolCategory, data: ExchangeRateData): ToolCategory {
  const rateLines = Object.entries(data.rates)
    .slice(0, 8)
    .map(([code, rate]) => `1 CNY ≈ ${rate.toFixed(4)} ${code}`);

  const dateStr = new Date(data.updatedAt).toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });

  return {
    ...category,
    sections: [
      {
        title: `Live rates (CNY base · updated ${dateStr})`,
        items: rateLines,
      },
      ...category.sections.filter((s) => s.title !== "Rate checks"),
    ],
    apiPriority: "Live: exchange-rate data connected via ExchangeRate-API (hourly refresh).",
  };
}

export function createLiveToolsProvider(): ToolsProvider {
  const staticProvider = createStaticToolsProvider();

  return {
    id: "live-tools",

    async getProviderStatus(): Promise<ToolsProviderStatus> {
      const liveRates = await fetchLiveRates();
      const base = await staticProvider.getProviderStatus();
      if (!liveRates) return base;
      return {
        ...base,
        id: "live-tools",
        label: "Live tools provider (exchange rate connected)",
        mode: "live",
        nextIntegration: "Visa-rule or transit data is the next candidate after exchange-rate is validated.",
        limitations: base.limitations.filter((l) => !l.toLowerCase().includes("exchange rate")),
      };
    },

    async listCategories(): Promise<ToolCategory[]> {
      const [staticCategories, liveRates] = await Promise.all([
        staticProvider.listCategories(),
        fetchLiveRates(),
      ]);

      if (!liveRates) return staticCategories;

      return staticCategories.map((cat) =>
        cat.id === "currency" ? injectLiveRates(cat, liveRates) : cat
      );
    },
  };
}
