"use client";

import { CheckCheck, Copy, ExternalLink, History, Mic, Paperclip, Pin, Send, Sparkles, Star, ThumbsDown, ThumbsUp, X } from "lucide-react";
import { useEffect, useState } from "react";
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
  const [slowWait, setSlowWait] = useState(false);

  // Some providers (Kimi K2.6's reasoning pass cannot be disabled — see
  // modelRegistry.ts minTimeoutMs/minMaxTokens, v0.3.19) can legitimately take
  // 60-90s to answer. The thinking indicator below is always shown while
  // busy, but its copy escalates after a threshold so a long real wait still
  // reads as "working" rather than "frozen."
  useEffect(() => {
    if (!busy) {
      setSlowWait(false);
      return;
    }
    const timer = setTimeout(() => setSlowWait(true), 15000);
    return () => clearTimeout(timer);
  }, [busy]);

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
      ? [
          message.response.headline,
          message.response.body,
          ...message.response.highlights,
          ...(message.response.toolCards ?? []).flatMap((card) => [card.title, card.summary, ...card.items]),
        ]
          .filter(Boolean)
          .join("\n")
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
            <article className="chat-message" data-role={message.role} data-error={message.isError ? "true" : undefined} key={message.id}>
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
                  {message.response.toolCards?.length ? (
                    <div className="chat-message__tool-cards" aria-label="Helpful travel tools">
                      {message.response.toolCards.map((card) => (
                        <article className="chat-tool-card" data-tone={card.tone ?? "info"} key={card.id}>
                          <div className="chat-tool-card__head">
                            <div>
                              {card.sourceLabel ? <span>{card.sourceLabel}</span> : null}
                              <strong>{card.title}</strong>
                            </div>
                            {card.href ? (
                              <a aria-label={`Open ${card.title}`} href={card.href} title={card.nextAction}>
                                <ExternalLink aria-hidden="true" size={14} strokeWidth={1.8} />
                              </a>
                            ) : null}
                          </div>
                          <p>{card.summary}</p>
                          <ul>
                            {card.items.slice(0, 4).map((item) => (
                              <li key={item}>{item}</li>
                            ))}
                          </ul>
                          {card.href ? (
                            <a className="chat-tool-card__action" href={card.href}>
                              {card.nextAction}
                            </a>
                          ) : (
                            <span className="chat-tool-card__action">{card.nextAction}</span>
                          )}
                        </article>
                      ))}
                    </div>
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
        {busy ? (
          <article className="chat-message chat-message--thinking" data-role="assistant" aria-live="polite">
            <div className="chat-message__byline">
              <img alt="" aria-hidden="true" className="chat-message__avatar" src="/visepanda-logo-icon.jpg" />
              <span>VisePanda</span>
            </div>
            <div className="chat-thinking">
              <span aria-hidden="true" />
              <span aria-hidden="true" />
              <span aria-hidden="true" />
              <p>
                {slowWait
                  ? "Still working — one of the travel details is taking longer to double-check than usual..."
                  : "Checking the practical travel details..."}
              </p>
            </div>
          </article>
        ) : null}
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
