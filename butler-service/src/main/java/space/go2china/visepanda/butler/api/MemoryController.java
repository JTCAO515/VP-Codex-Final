package space.go2china.visepanda.butler.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import space.go2china.visepanda.butler.memory.TripMemoryStore;
import space.go2china.visepanda.butler.memory.UserMemoryEntry;
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

    // View/delete surface for the "your profile" screen (Issue #14) — entries
    // the memory system inferred or was explicitly told, with evidence/source
    // so the traveler can see why an entry exists before deciding to remove it.
    @GetMapping("/butler/memory/profile")
    public ResponseEntity<Map<String, Object>> listProfile(@RequestParam String userId) {
        if (blank(userId)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "user_id_required"));
        }
        List<UserMemoryEntry> entries = userProfileStore.listAll("user:" + userId.trim());
        return ResponseEntity.ok(Map.of("ok", true, "entries", entries));
    }

    @DeleteMapping("/butler/memory/profile")
    public ResponseEntity<Map<String, Object>> deleteProfileEntry(
            @RequestParam String userId, @RequestParam String key, @RequestParam String value) {
        if (blank(userId) || blank(key) || blank(value)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "user_id_key_value_required"));
        }
        boolean removed = userProfileStore.delete("user:" + userId.trim(), key.trim(), value.trim());
        return ResponseEntity.ok(Map.of("ok", true, "removed", removed));
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
