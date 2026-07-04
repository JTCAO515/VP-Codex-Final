package space.go2china.visepanda.butler.persistence;

import space.go2china.visepanda.butler.memory.MemoryContext;

public interface ChatLogSink {
    void save(MemoryContext context, String userMessage, String assistantMessage);
}
