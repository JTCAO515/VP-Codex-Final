package space.go2china.visepanda.butler.agent;

import dev.langchain4j.service.UserMessage;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.TripState;

public interface TripPlannerAiService {
    @UserMessage("""
            Return a VisePanda CanvasPatch JSON object for this traveler message.
            Message: {{message}}
            Current trip: {{trip}}
            """)
    CanvasPatch plan(String message, TripState trip);
}
