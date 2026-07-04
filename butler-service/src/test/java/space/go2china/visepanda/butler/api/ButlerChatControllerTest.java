package space.go2china.visepanda.butler.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"DASHSCOPE_API_KEY=", "ZHIPU_API_KEY=", "MOONSHOT_API_KEY="})
@AutoConfigureMockMvc
class ButlerChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns503WithoutAnyLlmProviderKey() throws Exception {
        mockMvc.perform(post("/butler/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Plan me a 3 day trip to Beijing",
                                  "trip": {
                                    "summary": {
                                      "title": "China Trip Draft",
                                      "durationDays": 5,
                                      "pace": "Balanced",
                                      "travelerStyle": "First-time visitor",
                                      "destinations": ["Beijing", "Shanghai"],
                                      "confidence": "Draft"
                                    },
                                    "days": [],
                                    "alerts": [],
                                    "lastUpdatedReason": "Initial VisePanda travel draft."
                                  }
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error").value("butler_unavailable"))
                .andExpect(jsonPath("$.message").value("No Chinese LLM provider is configured."));
    }

    @Test
    void rejectsMissingTrip() throws Exception {
        mockMvc.perform(post("/butler/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error").value("trip_required"));
    }
}
