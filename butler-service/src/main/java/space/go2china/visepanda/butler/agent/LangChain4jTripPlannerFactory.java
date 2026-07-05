package space.go2china.visepanda.butler.agent;

import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

@Component
public class LangChain4jTripPlannerFactory {
    public Class<?> aiServicesType() {
        return AiServices.class;
    }
}
