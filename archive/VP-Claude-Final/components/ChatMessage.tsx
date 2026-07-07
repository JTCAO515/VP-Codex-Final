import type { ChatMessage as ChatMessageType } from "@/lib/types";

export function ChatMessage({ message }: { message: ChatMessageType }) {
  const isUser = message.role === "user";
  return (
    <div className={`mb-3 ${isUser ? "text-right" : "text-left"}`}>
      <p className={`whitespace-pre-wrap text-sm ${isUser ? "text-ink-umber/70" : "text-ink-umber"}`}>
        {message.content}
      </p>
    </div>
  );
}
