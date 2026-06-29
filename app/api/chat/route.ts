import { NextRequest } from "next/server";
import { generateMockReply } from "@/lib/mock-ai";
import { deepSeekSseToTextStream, textToChunkStream } from "@/lib/streaming";
import type { ChatMessage, DayCard } from "@/lib/types";

export const runtime = "nodejs";

interface ChatRequestBody {
  messages: ChatMessage[];
  days: DayCard[];
}

function buildSystemPrompt(): string {
  return [
    "You are VisePanda, an AI travel butler for foreign tourists visiting China.",
    "Always answer the traveler's question in plain conversational text first.",
    "Then, only if this turn should change the itinerary, append a fenced code block",
    'tagged exactly ```json-trip-instructions``` containing a single JSON object',
    'with optional "days", "rails", and "summary" fields describing changes to the itinerary canvas.',
    "Each day instruction has shape {day, action: 'upsert'|'delete', data?: {city, activities: [{period: 'morning'|'afternoon'|'evening', title, imageHint}], food, hotel, transport, pace, budgetNote}}.",
    "Each rail instruction has shape {id, action: 'upsert'|'delete', data?: {category, title, detail, severity}}.",
    '"summary" is a partial object merged into the trip summary: {route: string[], startDate, endDate, travelers, days}.',
    "Omit the code block entirely when nothing about the itinerary should change this turn.",
  ].join(" ");
}

function mockResponseStream(lastUserMessage: string, days: DayCard[]): ReadableStream<Uint8Array> {
  const mock = generateMockReply(lastUserMessage, days);
  const fenced = `${mock.chatText}\n\n\`\`\`json-trip-instructions\n${JSON.stringify(mock.instructions)}\n\`\`\``;
  return textToChunkStream(fenced);
}

export async function POST(req: NextRequest) {
  const body = (await req.json()) as ChatRequestBody;
  const lastUserMessage = body.messages[body.messages.length - 1]?.content ?? "";
  const apiKey = process.env.DEEPSEEK_API_KEY;

  if (!apiKey) {
    return new Response(mockResponseStream(lastUserMessage, body.days), {
      headers: { "Content-Type": "text/plain; charset=utf-8" },
    });
  }

  const upstream = await fetch("https://api.deepseek.com/chat/completions", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model: "deepseek-chat",
      stream: true,
      messages: [
        { role: "system", content: buildSystemPrompt() },
        ...body.messages.map((m) => ({ role: m.role, content: m.content })),
      ],
    }),
  });

  if (!upstream.ok || !upstream.body) {
    return new Response(mockResponseStream(lastUserMessage, body.days), {
      headers: { "Content-Type": "text/plain; charset=utf-8" },
    });
  }

  return new Response(deepSeekSseToTextStream(upstream.body), {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}
