"use client";

import { CheckCheck, Copy, History, Mic, Paperclip, Pin, Send, Sparkles, Star, ThumbsDown, ThumbsUp, X } from "lucide-react";
import { useState } from "react";
import type { ChangeEvent, FormEvent, KeyboardEvent } from "react";
import { ChangeDigestCard } from "@/components/canvas/ChangeDigestCard";
import type { ChatMessage } from "@/lib/types/trip";

interface ChatPanelProps {
  messages: ChatMessage[];
  onSend: (message: string) => void | Promise<void>;
  suggestions: string[];
  profileChips?: string[];
  busy?: boolean;
  onSelectDay?: (dayNumber: number) => void;
  onUndo?: () => void;
  undoMessageId?: string;
}

function formatTimestamp(iso?: string): string | null {
  if (!iso) return null;
  try {
    return new Date(iso).toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" });
  } catch {
    return null;
  }
}

export function ChatPanel({
  messages,
  onSend,
  suggestions,
  profileChips = [],
  busy = false,
  onSelectDay,
  onUndo,
  undoMessageId,
}: ChatPanelProps) {
  const [draft, setDraft] = useState("");
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<Record<string, "up" | "down">>({});
  const [dismissedNextStepId, setDismissedNextStepId] = useState<string | null>(null);

  const lastAssistantWithNextStep = [...messages].reverse().find((message) => message.role === "assistant" && message.response?.nextStep);
  const latestNextStep =
    lastAssistantWithNextStep?.id !== dismissedNextStepId ? lastAssistantWithNextStep?.response?.nextStep : undefined;
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

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      submitMessage(draft);
    }
  }

  async function handleCopy(message: ChatMessage) {
    const text = message.response
      ? [message.response.headline, message.response.body, ...message.response.highlights].filter(Boolean).join("\n")
      : message.content;
    try {
      await navigator.clipboard.writeText(text);
      setCopiedId(message.id);
      setTimeout(() => setCopiedId((current) => (current === message.id ? null : current)), 1600);
    } catch {
      // Clipboard access can be denied by the browser; failing silently is fine — copy is a convenience, not a core action.
    }
  }

  function handleFeedback(messageId: string, vote: "up" | "down") {
    setFeedback((current) => ({ ...current, [messageId]: current[messageId] === vote ? undefined : vote }) as Record<string, "up" | "down">);
  }

  return (
    <aside className="chat-panel" aria-label="AI butler chat">
      <div className="chat-panel__head">
        <div className="chat-panel__head-title">
          <img alt="" aria-hidden="true" className="chat-panel__avatar" src="/visepanda-logo-icon.jpg" />
          <p className="section-kicker">Ask VisePanda</p>
        </div>
        <div className="chat-panel__head-actions">
          <a aria-label="View saved trips" href="/trips" title="Trip history">
            <History aria-hidden="true" size={16} strokeWidth={1.8} />
          </a>
          <button aria-label="Pin this conversation" disabled title="Coming soon" type="button">
            <Pin aria-hidden="true" size={16} strokeWidth={1.8} />
          </button>
        </div>
      </div>
      {profileChips.length > 0 ? (
        <div className="chat-profile-chips" aria-label="Remembered preferences">
          {profileChips.slice(0, 6).map((chip) => (
            <span key={chip}>{chip}</span>
          ))}
        </div>
      ) : null}
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
        {messages.map((message) => {
          const timestamp = formatTimestamp(message.createdAt);
          return (
            <article className="chat-message" data-role={message.role} key={message.id}>
              <div className="chat-message__byline">
                {message.role === "assistant" ? (
                  <img alt="" aria-hidden="true" className="chat-message__avatar" src="/visepanda-logo-icon.jpg" />
                ) : null}
                <span>{message.role === "assistant" ? "VisePanda" : "You"}</span>
                {timestamp ? <time dateTime={message.createdAt}>{timestamp}</time> : null}
                {message.role === "user" ? <CheckCheck aria-hidden="true" className="chat-message__sent" size={13} strokeWidth={2} /> : null}
              </div>
              {message.role === "assistant" && message.response ? (
                <div className="chat-message__response">
                  <strong>{message.response.headline}</strong>
                  {message.response.body ? <p>{message.response.body}</p> : null}
                  {message.response.highlights.length > 0 ? (
                    <div className="chat-message__highlights">
                      {message.response.highlights.map((highlight) => (
                        <div className="chat-message__highlight" key={highlight}>
                          <Sparkles aria-hidden="true" size={14} strokeWidth={1.8} />
                          <span>{highlight}</span>
                        </div>
                      ))}
                    </div>
                  ) : null}
                  {message.response.watchOut ? (
                    <p className="chat-message__watch">Watch out: {message.response.watchOut}</p>
                  ) : null}
                  <p className="chat-message__next">{message.response.nextStep}</p>
                  <div className="chat-message__feedback">
                    <button
                      aria-label="Good response"
                      aria-pressed={feedback[message.id] === "up"}
                      onClick={() => handleFeedback(message.id, "up")}
                      type="button"
                    >
                      <ThumbsUp aria-hidden="true" size={14} strokeWidth={1.8} />
                    </button>
                    <button
                      aria-label="Not helpful"
                      aria-pressed={feedback[message.id] === "down"}
                      onClick={() => handleFeedback(message.id, "down")}
                      type="button"
                    >
                      <ThumbsDown aria-hidden="true" size={14} strokeWidth={1.8} />
                    </button>
                    <button aria-label="Copy response" onClick={() => handleCopy(message)} type="button">
                      <Copy aria-hidden="true" size={14} strokeWidth={1.8} />
                    </button>
                    {copiedId === message.id ? <span className="chat-message__copied">Copied</span> : null}
                  </div>
                </div>
              ) : (
                <p>{message.content}</p>
              )}
              {message.role === "assistant" && message.changeDigest?.length ? (
                <ChangeDigestCard
                  entries={message.changeDigest}
                  onSelectDay={onSelectDay}
                  onUndo={message.id === undoMessageId ? onUndo : undefined}
                />
              ) : null}
            </article>
          );
        })}
      </div>
      {latestNextStep ? (
        <div className="chat-next-step-card" aria-label="Primary next step">
          <Star aria-hidden="true" className="chat-next-step-card__icon" size={16} strokeWidth={1.8} />
          <div className="chat-next-step-card__body">
            <span>Next step</span>
            <button type="button" onClick={() => submitMessage(latestNextStep)} disabled={busy}>
              {latestNextStep}
            </button>
          </div>
          <button
            aria-label="Dismiss next step"
            className="chat-next-step-card__dismiss"
            onClick={() => setDismissedNextStepId(lastAssistantWithNextStep?.id ?? null)}
            type="button"
          >
            <X aria-hidden="true" size={14} strokeWidth={1.8} />
          </button>
        </div>
      ) : null}
      <form className="chat-composer" onSubmit={handleSubmit}>
        <label htmlFor="butler-message">Ask VisePanda</label>
        <div className="chat-composer__row">
          <button aria-label="Attach a file" className="chat-composer__icon-button" disabled title="Coming soon" type="button">
            <Paperclip aria-hidden="true" size={17} strokeWidth={1.8} />
          </button>
          <textarea
            id="butler-message"
            value={draft}
            onChange={handleDraftChange}
            onInput={handleDraftChange}
            onKeyDown={handleKeyDown}
            onBlur={handleDraftChange}
            placeholder="Ask VisePanda anything..."
            rows={1}
          />
          <button aria-label="Voice input" className="chat-composer__icon-button" disabled title="Coming soon" type="button">
            <Mic aria-hidden="true" size={17} strokeWidth={1.8} />
          </button>
          <button aria-label="Send" className="chat-composer__send" disabled={busy || !draft.trim()} type="submit">
            <Send aria-hidden="true" size={16} strokeWidth={2} />
          </button>
        </div>
      </form>
      <p className="chat-disclaimer">VisePanda can make mistakes. Please double-check important details.</p>
    </aside>
  );
}
