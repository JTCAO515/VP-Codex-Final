"use client";

import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import type { ChatMessage } from "@/lib/types/trip";

interface ChatPanelProps {
  messages: ChatMessage[];
  onSend: (message: string) => void | Promise<void>;
  suggestions: string[];
  profileChips?: string[];
  busy?: boolean;
}

export function ChatPanel({ messages, onSend, suggestions, profileChips = [], busy = false }: ChatPanelProps) {
  const [draft, setDraft] = useState("");
  const latestNextStep = [...messages].reverse().find((message) => message.role === "assistant" && message.response?.nextStep)?.response?.nextStep;
  const promptLabel = messages.length === 0 ? "First trip starts" : "Suggested prompts";
  const visibleSuggestions = messages.length === 0 ? suggestions.slice(0, 3) : suggestions;
  const isFirstRun = messages.length === 0;

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
        {profileChips.length > 0 ? (
          <div className="chat-profile-chips" aria-label="Remembered preferences">
            {profileChips.slice(0, 5).map((chip) => (
              <span key={chip}>{chip}</span>
            ))}
          </div>
        ) : null}
      </div>
      {isFirstRun ? (
        <div className="chat-empty-state" aria-label="Chat starter state">
          <strong>Start with a ready-made China route</strong>
          <span>Independent, practical, and easy to refine.</span>
        </div>
      ) : null}
      <div className="prompt-row" aria-label={promptLabel}>
        {visibleSuggestions.map((prompt) => (
          <button key={prompt} type="button" onClick={() => submitMessage(prompt)} disabled={busy}>
            {prompt}
          </button>
        ))}
      </div>
      <div className="chat-log" aria-label="Conversation">
        {messages.map((message) => (
          <article className="chat-message" data-role={message.role} key={message.id}>
            <span>{message.role === "assistant" ? "VisePanda" : "You"}</span>
            {message.role === "assistant" && message.response ? (
              <div className="chat-message__response">
                <strong>{message.response.headline}</strong>
                {message.response.body ? <p>{message.response.body}</p> : null}
                {message.response.highlights.length > 0 ? (
                  <ul>
                    {message.response.highlights.map((highlight) => (
                      <li key={highlight}>{highlight}</li>
                    ))}
                  </ul>
                ) : null}
                {message.response.watchOut ? (
                  <p className="chat-message__watch">Watch out: {message.response.watchOut}</p>
                ) : null}
                <p className="chat-message__next">{message.response.nextStep}</p>
              </div>
            ) : (
              <p>{message.content}</p>
            )}
          </article>
        ))}
      </div>
      {latestNextStep ? (
        <div className="chat-next-step-card" aria-label="Primary next step">
          <span>Next step</span>
          <button type="button" onClick={() => submitMessage(latestNextStep)} disabled={busy}>
            {latestNextStep}
          </button>
        </div>
      ) : null}
      <form className="chat-composer" onSubmit={handleSubmit}>
        <label htmlFor="butler-message">Ask VisePanda</label>
        <textarea
          id="butler-message"
          value={draft}
          onChange={handleDraftChange}
          onInput={handleDraftChange}
          onKeyUp={handleDraftChange}
          onBlur={handleDraftChange}
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
