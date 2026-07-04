package space.go2china.visepanda.butler.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import space.go2china.visepanda.butler.agent.ButlerIntent;
import space.go2china.visepanda.butler.agent.RouterAgent;
import space.go2china.visepanda.butler.agent.TripPlannerAgent;
import space.go2china.visepanda.butler.model.ChatRequest;
import space.go2china.visepanda.butler.model.ChatResponse;
import space.go2china.visepanda.butler.model.TripState;

@RestController
public class ButlerChatController {
    private final RouterAgent routerAgent;
    private final TripPlannerAgent tripPlannerAgent;

    public ButlerChatController(RouterAgent routerAgent, TripPlannerAgent tripPlannerAgent) {
        this.routerAgent = routerAgent;
        this.tripPlannerAgent = tripPlannerAgent;
    }

    @PostMapping("/butler/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ResponseEntity<ChatResponse> invalid = validate(request);
        if (invalid != null) return invalid;

        ButlerIntent intent = routerAgent.classify(request.message());
        return ResponseEntity.ok(tripPlannerAgent.plan(intent, request.message(), request.trip()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ChatResponse.error("butler_unavailable", exception.getMessage()));
    }

    private ResponseEntity<ChatResponse> validate(ChatRequest request) {
        if (request == null || request.message() == null || request.message().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ChatResponse.error("message_required", "message is required"));
        }
        TripState trip = request.trip();
        if (trip == null || trip.summary() == null || trip.days() == null || trip.alerts() == null) {
            return ResponseEntity.badRequest().body(ChatResponse.error("trip_required", "trip.summary, trip.days, and trip.alerts are required"));
        }
        return null;
    }
}
