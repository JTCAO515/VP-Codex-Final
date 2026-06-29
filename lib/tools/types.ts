export interface ToolCategory {
  id: string;
  name: string;
  summary: string;
  tips: string[];
}

export interface ToolsProvider {
  id: string;
  listCategories(): Promise<ToolCategory[]>;
}
