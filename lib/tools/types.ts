export interface ToolCategory {
  id: string;
  name: string;
  summary: string;
  tips: string[];
  sections: Array<{
    title: string;
    items: string[];
  }>;
  offlineTips: string[];
  apiPriority: string;
  cta?: { label: string; href: string };
  interactive?: ToolInteractiveDescriptor;
}

export type ToolInteractiveDescriptor =
  | {
      type: "currency-converter";
      baseCurrency: "CNY";
      defaultTarget: string;
      commonAmounts: number[];
      fallbackRates: Record<string, number>;
    }
  | {
      type: "visa-checker";
      nationalities: Array<{
        id: string;
        label: string;
        visaFreeDays?: number;
        transitHours?: number;
        note: string;
      }>;
    }
  | {
      type: "payment-wizard";
      wallets: Array<{ id: string; label: string; appName: string }>;
      cardBrands: Array<{ id: string; label: string; note: string }>;
    };

export interface ToolsProviderStatus {
  id: string;
  label: string;
  mode: "static" | "live";
  coverage: string;
  candidates: string[];
  nextIntegration: string;
  limitations: string[];
}

export interface ToolsProvider {
  id: string;
  getProviderStatus(): Promise<ToolsProviderStatus>;
  listCategories(): Promise<ToolCategory[]>;
}
