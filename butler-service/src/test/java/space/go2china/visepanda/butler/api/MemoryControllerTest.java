package space.go2china.visepanda.butler.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import space.go2china.visepanda.butler.memory.TripMemoryStore;
import space.go2china.visepanda.butler.memory.UserMemoryEntry;
import space.go2china.visepanda.butler.memory.UserProfileStore;

@SpringBootTest
@AutoConfigureMockMvc
class MemoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileStore userProfileStore;

    @MockBean
    private TripMemoryStore tripMemoryStore;

    @Test
    void listsProfileEntriesForAUser() throws Exception {
        when(userProfileStore.listAll("user:traveler1")).thenReturn(List.of(
                new UserMemoryEntry("dietary", "vegetarian", 0.95, List.of("I am vegetarian"), "explicit", Instant.now())));

        mockMvc.perform(get("/butler/memory/profile").param("userId", "traveler1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.entries[0].key").value("dietary"))
                .andExpect(jsonPath("$.entries[0].value").value("vegetarian"))
                .andExpect(jsonPath("$.entries[0].source").value("explicit"));
    }

    @Test
    void listProfileRejectsMissingUserId() throws Exception {
        mockMvc.perform(get("/butler/memory/profile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProfileEntryReportsWhatTheStoreReturned() throws Exception {
        when(userProfileStore.delete("user:traveler1", "dietary", "vegetarian")).thenReturn(true);

        mockMvc.perform(delete("/butler/memory/profile")
                        .param("userId", "traveler1")
                        .param("key", "dietary")
                        .param("value", "vegetarian"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.removed").value(true));

        verify(userProfileStore).delete(eq("user:traveler1"), eq("dietary"), eq("vegetarian"));
    }

    @Test
    void deleteProfileEntryRejectsMissingParams() throws Exception {
        mockMvc.perform(delete("/butler/memory/profile").param("userId", "someone"))
                .andExpect(status().isBadRequest());
    }
}
