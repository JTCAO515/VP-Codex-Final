package space.go2china.visepanda.butler.memory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.persistence.ChatLogSink;

@Component
public class MemoryAgent {
    private final ChatMemory chatMemory;
    private final PreferenceSignalExtractor extractor;
    private final UserProfileStore userProfileStore;
    private final TripMemoryStore tripMemoryStore;
    private final ChatLogSink chatLogSink;
    private final PiiSanitizer sanitizer;

    public MemoryAgent(ChatMemory chatMemory, PreferenceSignalExtractor extractor, UserProfileStore userProfileStore,
                       TripMemoryStore tripMemoryStore, ChatLogSink chatLogSink, PiiSanitizer sanitizer) {
        this.chatMemory = chatMemory;
        this.extractor = extractor;
        this.userProfileStore = userProfileStore;
        this.tripMemoryStore = tripMemoryStore;
        this.chatLogSink = chatLogSink;
        this.sanitizer = sanitizer;
    }

    @Async
    public void afterTurn(MemoryContext context, String userMessage, CanvasPatch patch) {
        String safeUser = sanitizer.sanitize(userMessage);
        String safeAssistant = sanitizer.sanitize(patch.assistantMessage());
        chatMemory.addExchange(context.sessionKey(), safeUser, safeAssistant);
        userProfileStore.applySignals(context.userKey(), extractor.extract(safeUser));
        tripMemoryStore.record(context.tripKey(), patch.intent(), patch.reason());
        chatLogSink.save(context, safeUser, safeAssistant);
    }
}
