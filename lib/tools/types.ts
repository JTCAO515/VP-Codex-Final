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
}

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
