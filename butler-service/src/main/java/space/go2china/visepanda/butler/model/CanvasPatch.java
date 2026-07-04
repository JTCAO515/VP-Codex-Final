package space.go2china.visepanda.butler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CanvasPatch(
        String intent,
        String assistantMessage,
        AssistantResponse assistantResponse,
        TripSummary tripSummary,
        List<TripDay> days,
        List<ButlerAlert> butlerAlerts,
        String reason
) {
}
