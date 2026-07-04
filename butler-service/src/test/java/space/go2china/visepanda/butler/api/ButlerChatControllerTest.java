package space.go2china.visepanda.butler.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "butler.llm.api-key=")
@AutoConfigureMockMvc
class ButlerChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsMockCanvasPatchWithoutApiKey() throws Exception {
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.mode").value("mock"))
                .andExpect(jsonPath("$.modelLabel").value("Mock Butler"))
                .andExpect(jsonPath("$.intent").value("create_trip"))
                .andExpect(jsonPath("$.strategy").value("mock-fallback"))
                .andExpect(jsonPath("$.providersTried[0]").value("mock"))
                .andExpect(jsonPath("$.patch.intent").value("create_trip"))
                .andExpect(jsonPath("$.patch.assistantMessage").isString())
                .andExpect(jsonPath("$.patch.assistantResponse.headline").isString())
                .andExpect(jsonPath("$.patch.assistantResponse.body").isString())
                .andExpect(jsonPath("$.patch.assistantResponse.highlights").isArray())
                .andExpect(jsonPath("$.patch.assistantResponse.nextStep").isString())
                .andExpect(jsonPath("$.patch.reason").isString())
                .andExpect(jsonPath("$.patch.tripSummary.title").value("Beijing Trip"))
                .andExpect(jsonPath("$.patch.days", hasSize(3)))
                .andExpect(jsonPath("$.patch.days[0].blocks[0].title").isString())
                .andExpect(jsonPath("$.patch.butlerAlerts", hasSize(2)))
                .andExpect(jsonPath("$.suggestions").isArray());
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
