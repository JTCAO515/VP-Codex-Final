"use client";

import { forwardRef, useState } from "react";
import { useTripStore } from "@/lib/store";
import { sendUserMessage } from "@/lib/send-message";
import { ChatMessage } from "./ChatMessage";

const QUICK_REPLIES = ["Less tiring", "Food-focused", "Adjust pace", "Add a day"];

export const ChatPanel = forwardRef<HTMLInputElement>(function ChatPanel(_props, inputRef) {
  const messages = useTripStore((state) => state.messages);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);

  async function submit(content: string) {
    const trimmed = content.trim();
    if (!trimmed || sending) return;
    setInput("");
    setSending(true);
    try {
      await sendUserMessage(trimmed);
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="flex h-full flex-col">
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {messages.length === 0 ? (
          <p className="text-sm text-ink-umber/70">
            Tell me where you&apos;re from, where you want to go, and how many days you have.
          </p>
        ) : (
          messages.map((message) => <ChatMessage key={message.id} message={message} />)
        )}
      </div>

      <div className="flex flex-wrap gap-2 px-4 pb-2">
        {QUICK_REPLIES.map((label) => (
          <button
            key={label}
            type="button"
            onClick={() => submit(label)}
            disabled={sending}
            className="rounded-full border border-ink-cinnabar/40 px-3 py-1 text-sm text-ink-cinnabar disabled:opacity-50"
          >
            {label}
          </button>
        ))}
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          submit(input);
        }}
        className="flex items-center gap-2 border-t border-ink-umber/15 px-4 py-3"
      >
        <span title="Coming soon" className="cursor-not-allowed text-ink-umber/40">
          📎
        </span>
        <span title="Coming soon" className="cursor-not-allowed text-ink-umber/40">
          🖼
        </span>
        <input
          ref={inputRef}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask anything about your China trip..."
          className="flex-1 rounded border border-ink-umber/15 bg-ink-paper px-3 py-2 text-sm text-ink-umber placeholder:text-ink-umber/50 focus:outline-none"
        />
        <button
          type="submit"
          disabled={sending}
          className="rounded bg-ink-cinnabar px-4 py-2 text-sm font-semibold text-ink-paper disabled:opacity-50"
        >
          Send
        </button>
      </form>
    </div>
  );
});
