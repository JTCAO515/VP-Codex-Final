export function textToChunkStream(text: string, delayMs = 25): ReadableStream<Uint8Array> {
  const words = text.split(/(\s+)/).filter((w) => w.length > 0);
  const encoder = new TextEncoder();
  let index = 0;

  return new ReadableStream<Uint8Array>({
    async pull(controller) {
      if (index >= words.length) {
        controller.close();
        return;
      }
      controller.enqueue(encoder.encode(words[index]));
      index += 1;
      if (delayMs > 0) {
        await new Promise((resolve) => setTimeout(resolve, delayMs));
      }
    },
  });
}

export function deepSeekSseToTextStream(
  upstream: ReadableStream<Uint8Array>
): ReadableStream<Uint8Array> {
  const reader = upstream.getReader();
  const decoder = new TextDecoder();
  const encoder = new TextEncoder();
  let buffer = "";
  let upstreamDone = false;

  return new ReadableStream<Uint8Array>({
    async pull(controller) {
      // Keep pulling from upstream until we get a complete line to process
      while (true) {
        // Try to extract complete lines from buffer
        const newlineIndex = buffer.indexOf("\n");
        if (newlineIndex !== -1) {
          // We have at least one complete line
          const line = buffer.substring(0, newlineIndex);
          buffer = buffer.substring(newlineIndex + 1);

          const trimmed = line.trim();
          if (trimmed.startsWith("data:")) {
            const payload = trimmed.slice("data:".length).trim();
            if (payload !== "[DONE]") {
              try {
                const json = JSON.parse(payload);
                const delta = json.choices?.[0]?.delta?.content;
                if (typeof delta === "string" && delta.length > 0) {
                  controller.enqueue(encoder.encode(delta));
                  return; // Return after enqueueing to let consumer read
                }
              } catch {
                // Skip malformed SSE lines rather than failing the stream.
              }
            }
          }
          // Loop to process next line
          continue;
        }

        // No complete line in buffer yet
        if (upstreamDone) {
          // Upstream is done, close the stream
          controller.close();
          return;
        }

        // Try to get more data from upstream
        const { done, value } = await reader.read();
        if (done) {
          upstreamDone = true;
          // Loop will check upstreamDone and close on next iteration
          continue;
        }

        buffer += decoder.decode(value, { stream: true });
        // Loop to try extracting lines again
      }
    },
  });
}
