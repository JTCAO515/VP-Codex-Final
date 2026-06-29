import { createStaticToolsProvider } from "@/lib/tools/staticProvider";
import type { ToolsProvider } from "@/lib/tools/types";

export function getToolsProvider(): ToolsProvider {
  return createStaticToolsProvider();
}

export type { ToolCategory, ToolsProvider } from "@/lib/tools/types";
