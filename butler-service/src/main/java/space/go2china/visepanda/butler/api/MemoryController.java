package space.go2china.visepanda.butler.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import space.go2china.visepanda.butler.memory.TripMemoryStore;
import space.go2china.visepanda.butler.memory.UserProfileStore;

@RestController
public class MemoryController {
    private final UserProfileStore userProfileStore;
    private final TripMemoryStore tripMemoryStore;

    public MemoryController(UserProfileStore userProfileStore, TripMemoryStore tripMemoryStore) {
        this.userProfileStore = userProfileStore;
        this.tripMemoryStore = tripMemoryStore;
    }

    @PostMapping("/butler/memory/migrate")
    public ResponseEntity<Map<String, Object>> migrate(@RequestBody MemoryMigrationRequest request) {
        if (blank(request.guestId()) || blank(request.userId())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "guest_and_user_required"));
        }
        userProfileStore.migrateGuestToUser("guest:" + request.guestId().trim(), "user:" + request.userId().trim());
        if (!blank(request.guestTripId()) && !blank(request.userTripId())) {
            tripMemoryStore.migrateGuestToUser(request.guestTripId().trim(), request.userTripId().trim());
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
