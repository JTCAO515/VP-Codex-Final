import { createLiveToolsProvider } from "@/lib/tools/liveToolsProvider";
import type { ToolsProvider } from "@/lib/tools/types";

export function getToolsProvider(): ToolsProvider {
  return createLiveToolsProvider();
}

export type { ToolCategory, ToolsProvider, ToolsProviderStatus } from "@/lib/tools/types";
