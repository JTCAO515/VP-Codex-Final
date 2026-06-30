"use client";

import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import type { ChatMessage } from "@/lib/types/trip";

interface ChatPanelProps {
  messages: ChatMessage[];
  onSend: (message: string) => void | Promise<void>;
  suggestions: string[];
  busy?: boolean;
}

export function ChatPanel({ messages, onSend, suggestions, busy = false }: ChatPanelProps) {
  const [draft, setDraft] = useState("");

  function submitMessage(message: string) {
    const trimmed = message.trim();
    if (!trimmed || busy) return;
    onSend(trimmed);
    setDraft("");
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    submitMessage(draft);
  }

  function handleDraftChange(event: ChangeEvent<HTMLTextAreaElement> | FormEvent<HTMLTextAreaElement>) {
    setDraft(event.currentTarget.value);
  }

  return (
    <aside className="chat-panel" aria-label="AI butler chat">
      <div className="chat-panel__head">
        <p className="section-kicker">Ask VisePanda</p>
      </div>
      <div className="prompt-row" aria-label="Suggested prompts">
        {suggestions.map((prompt) => (
          <button key={prompt} type="button" onClick={() => submitMessage(prompt)} disabled={busy}>
            {prompt}
          </button>
        ))}
      </div>
      <div className="chat-log" aria-label="Conversation">
        {messages.map((message) => (
          <article className="chat-message" data-role={message.role} key={message.id}>
            <span>{message.role === "assistant" ? "VisePanda" : "You"}</span>
            <p>{message.content}</p>
          </article>
        ))}
      </div>
      <form className="chat-composer" onSubmit={handleSubmit}>
        <label htmlFor="butler-message">Ask VisePanda</label>
        <textarea
          id="butler-message"
          value={draft}
          onChange={handleDraftChange}
          onInput={handleDraftChange}
          placeholder="Tell me your days, cities, budget, pace, or worries..."
          rows={3}
        />
        <button type="submit" disabled={busy || !draft.trim()}>
          Send
        </button>
      </form>
    </aside>
  );
}
