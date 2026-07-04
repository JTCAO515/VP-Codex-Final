package space.go2china.visepanda.butler.context;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.agent.ExecutionPlan;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.model.ChatRequest;
import space.go2china.visepanda.butler.model.TripDay;

@Service
public class ContextInjectionService {
    public ContextSnapshot build(ChatRequest request, MemoryContext memory, ExecutionPlan plan) {
        TravelStage stage = inferStage(request);
        List<String> gaps = gaps(request.trip().days());
        int readiness = Math.max(20, Math.min(95, 100 - gaps.size() * 15 - request.trip().alerts().size() * 5));
        return new ContextSnapshot(stage, template(stage), request.currentCity(), request.currentDay(),
                request.entryPoint() == null ? "chat" : request.entryPoint(), plan.urgency(), readiness, gaps, memory.profile());
    }

    private TravelStage inferStage(ChatRequest request) {
        if (request.currentCity() != null && !request.currentCity().isBlank()) return TravelStage.IN_CHINA;
        if (request.departureDate() == null || request.departureDate().isBlank()) return TravelStage.PLANNING;
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(request.departureDate()));
            if (days <= 0) return TravelStage.IN_CHINA;
            if (days <= 14) return TravelStage.PREPARING;
        } catch (Exception ignored) {
            return TravelStage.PLANNING;
        }
        return TravelStage.PLANNING;
    }

    private StageTemplate template(TravelStage stage) {
        if (stage == TravelStage.IN_CHINA) return new StageTemplate("IN_CHINA_MODE", "short actionable lines", 4);
        return new StageTemplate("PLANNING_MODE", "structured planning detail", 8);
    }

    private List<String> gaps(List<TripDay> days) {
        List<String> gaps = new ArrayList<>();
        if (days == null || days.isEmpty()) return List.of("no scheduled days");
        for (TripDay day : days) {
            if (day.stay() == null || day.stay().isBlank()) gaps.add("Day " + day.day() + " stay missing");
            if (day.blocks() == null || day.blocks().isEmpty()) gaps.add("Day " + day.day() + " is empty");
        }
        return gaps;
    }
}
