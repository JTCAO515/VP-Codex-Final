package space.go2china.visepanda.butler.memory;

import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.agent.ButlerIntent;
import space.go2china.visepanda.butler.model.ChatRequest;

@Service
public class MemoryContextService {
    private final ChatMemory chatMemory;
    private final UserProfileStore userProfileStore;
    private final TripMemoryStore tripMemoryStore;

    public MemoryContextService(ChatMemory chatMemory, UserProfileStore userProfileStore, TripMemoryStore tripMemoryStore) {
        this.chatMemory = chatMemory;
        this.userProfileStore = userProfileStore;
        this.tripMemoryStore = tripMemoryStore;
    }

    public MemoryContext build(ChatRequest request, ButlerIntent intent) {
        String userKey = MemoryKeys.userKey(request);
        String sessionKey = MemoryKeys.sessionKey(request);
        String tripKey = MemoryKeys.tripKey(request);
        ChatMemorySnapshot chat = chatMemory.snapshot(sessionKey);
        return new MemoryContext(userKey, sessionKey, tripKey, chat.runningSummary(), chat.recentTurns(),
                userProfileStore.topK(userKey, intent.wireName(), 6), tripMemoryStore.recent(tripKey, 6));
    }
}
