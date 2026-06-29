import { describe, it, expect } from "vitest";
import { textToChunkStream, deepSeekSseToTextStream } from "./streaming";

async function readAllText(stream: ReadableStream<Uint8Array>): Promise<string> {
  const reader = stream.getReader();
  const decoder = new TextDecoder();
  let result = "";
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    result += decoder.decode(value, { stream: true });
  }
  return result;
}

function stringChunksToStream(chunks: string[]): ReadableStream<Uint8Array> {
  const encoder = new TextEncoder();
  let index = 0;
  return new ReadableStream<Uint8Array>({
    pull(controller) {
      if (index >= chunks.length) {
        controller.close();
        return;
      }
      controller.enqueue(encoder.encode(chunks[index]));
      index += 1;
    },
  });
}

describe("textToChunkStream", () => {
  it("reconstructs the original text when all chunks are read", async () => {
    const original = "Here is your 5-day itinerary across Beijing and Shanghai.";
    const text = await readAllText(textToChunkStream(original, 0));
    expect(text).toBe(original);
  });
});

describe("deepSeekSseToTextStream", () => {
  it("extracts delta content across multiple SSE events", async () => {
    const sse = [
      'data: {"choices":[{"delta":{"content":"Hello"}}]}\n\n',
      'data: {"choices":[{"delta":{"content":", world"}}]}\n\n',
      "data: [DONE]\n\n",
    ];
    const text = await readAllText(deepSeekSseToTextStream(stringChunksToStream(sse)));
    expect(text).toBe("Hello, world");
  });

  it("ignores malformed SSE lines instead of throwing", async () => {
    const sse = ["data: not json\n\n", 'data: {"choices":[{"delta":{"content":"ok"}}]}\n\n'];
    const text = await readAllText(deepSeekSseToTextStream(stringChunksToStream(sse)));
    expect(text).toBe("ok");
  });

  it("handles an SSE event split across two underlying chunks", async () => {
    const sse = ['data: {"choices":[{"delta"', ':{"content":"split"}}]}\n\n'];
    const text = await readAllText(deepSeekSseToTextStream(stringChunksToStream(sse)));
    expect(text).toBe("split");
  });
});
