package space.go2china.visepanda.butler.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import space.go2china.visepanda.butler.agent.ButlerOrchestrator;
import space.go2china.visepanda.butler.memory.MemoryAgent;
import space.go2china.visepanda.butler.model.ChatRequest;
import space.go2china.visepanda.butler.model.ChatResponse;
import space.go2china.visepanda.butler.model.TripState;

@RestController
public class ButlerChatController {
    private final ButlerOrchestrator orchestrator;
    private final MemoryAgent memoryAgent;

    public ButlerChatController(ButlerOrchestrator orchestrator, MemoryAgent memoryAgent) {
        this.orchestrator = orchestrator;
        this.memoryAgent = memoryAgent;
    }

    @PostMapping("/butler/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ResponseEntity<ChatResponse> invalid = validate(request);
        if (invalid != null) return invalid;

        ButlerOrchestrator.OrchestrationResponse response = orchestrator.run(request);
        memoryAgent.afterTurn(response.memoryContext(), request.message(), response.response().patch());
        return ResponseEntity.ok(response.response());
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
