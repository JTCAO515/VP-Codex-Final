import { parseAssistantReply } from "./parse-instructions";
import { useTripStore } from "./store";
import type { ChatMessage } from "./types";

export async function sendUserMessage(content: string): Promise<void> {
  const store = useTripStore.getState();
  const userMessage: ChatMessage = { id: crypto.randomUUID(), role: "user", content };
  const assistantMessage: ChatMessage = { id: crypto.randomUUID(), role: "assistant", content: "" };

  store.addMessage(userMessage);
  store.addMessage(assistantMessage);

  const latestState = useTripStore.getState();
  const response = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      messages: [...latestState.messages, userMessage],
      days: latestState.days,
    }),
  });

  if (!response.body) {
    return;
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let fullText = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    fullText += decoder.decode(value, { stream: true });
    const { chatText } = parseAssistantReply(fullText);
    const fenceMarkerIndex = chatText.indexOf("```json-trip-instructions");
    const displayText =
      fenceMarkerIndex === -1 ? chatText : chatText.slice(0, fenceMarkerIndex).trim();
    useTripStore.getState().updateMessageContent(assistantMessage.id, displayText);
  }

  const { chatText, instructions } = parseAssistantReply(fullText);
  useTripStore.getState().updateMessageContent(assistantMessage.id, chatText);
  if (instructions) {
    useTripStore.getState().applyInstructions(instructions);
  }
}
