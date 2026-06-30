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

export interface ToolsProvider {
  id: string;
  listCategories(): Promise<ToolCategory[]>;
}
