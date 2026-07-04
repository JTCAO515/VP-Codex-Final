package space.go2china.visepanda.butler.api;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"butler.llm.api-key=", "supabase.url=", "supabase.service-role-key="})
@AutoConfigureMockMvc
class PhaseCButlerChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void noKeyCompoundRequestReturnsMergedPlanAndBoundedToolLog() throws Exception {
        mockMvc.perform(post("/butler/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "phase-c-test",
                                  "guestId": "guest-c",
                                  "message": "Make Day 2 lighter and recommend nearby lunch",
                                  "trip": {
                                    "summary": {
                                      "title": "China Trip Draft",
                                      "durationDays": 3,
                                      "pace": "Balanced",
                                      "travelerStyle": "First-time visitor",
                                      "destinations": ["Beijing"],
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
                .andExpect(jsonPath("$.patch.assistantResponse.followUp").isArray())
                .andExpect(jsonPath("$.toolContext.executionPlan.steps[0].agent").value("TripPlannerAgent"))
                .andExpect(jsonPath("$.toolContext.executionPlan.steps[1].agent").value("LocalExpertAgent"))
                .andExpect(jsonPath("$.toolContext.executionLog[1].agent").value("LocalExpertAgent"))
                .andExpect(jsonPath("$.toolContext.toolCalls", lessThanOrEqualTo(6)))
                .andExpect(jsonPath("$.toolContext.maxDepth").value(2));
    }

    @Test
    void inChinaRequestUsesShortTemplate() throws Exception {
        mockMvc.perform(post("/butler/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "phase-c-china",
                                  "guestId": "guest-c",
                                  "currentCity": "Beijing",
                                  "currentDay": 2,
                                  "message": "How do I pay with Alipay?",
                                  "trip": {
                                    "summary": {
                                      "title": "China Trip Draft",
                                      "durationDays": 3,
                                      "pace": "Balanced",
                                      "travelerStyle": "First-time visitor",
                                      "destinations": ["Beijing"],
                                      "confidence": "Draft"
                                    },
                                    "days": [],
                                    "alerts": [],
                                    "lastUpdatedReason": "Initial VisePanda travel draft."
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolContext.context.stage").value("IN_CHINA"))
                .andExpect(jsonPath("$.toolContext.context.template.id").value("IN_CHINA_MODE"));
    }

    @Test
    void depthCapKeepsPartialResultsWhenPlanHasTooManySteps() throws Exception {
        mockMvc.perform(post("/butler/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "phase-c-depth",
                                  "guestId": "guest-c",
                                  "message": "Make Day 2 lighter, recommend nearby lunch, and translate hello into Chinese",
                                  "trip": {
                                    "summary": {
                                      "title": "China Trip Draft",
                                      "durationDays": 3,
                                      "pace": "Balanced",
                                      "travelerStyle": "First-time visitor",
                                      "destinations": ["Beijing"],
                                      "confidence": "Draft"
                                    },
                                    "days": [],
                                    "alerts": [],
                                    "lastUpdatedReason": "Initial VisePanda travel draft."
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolContext.executionPlan.steps[2].agent").value("TranslatorAgent"))
                .andExpect(jsonPath("$.toolContext.executionLog[0].agent").value("TripPlannerAgent"))
                .andExpect(jsonPath("$.toolContext.executionLog[1].agent").value("LocalExpertAgent"))
                .andExpect(jsonPath("$.toolContext.timedOut").value(true));
    }

    @Test
    void obscureFactDoesNotInventWhenRagMisses() throws Exception {
        mockMvc.perform(post("/butler/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "phase-c-rag-miss",
                                  "guestId": "guest-c",
                                  "message": "What is the Zhangmu lunar yak ferry permit requirement?",
                                  "trip": {
                                    "summary": {
                                      "title": "China Trip Draft",
                                      "durationDays": 3,
                                      "pace": "Balanced",
                                      "travelerStyle": "First-time visitor",
                                      "destinations": ["Beijing"],
                                      "confidence": "Draft"
                                    },
                                    "days": [],
                                    "alerts": [],
                                    "lastUpdatedReason": "Initial VisePanda travel draft."
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolContext.executionLog[0].agent").value("LogisticsAgent"))
                .andExpect(jsonPath("$.patch.assistantResponse.body").value(org.hamcrest.Matchers.containsString("I do not know")));
    }
}
