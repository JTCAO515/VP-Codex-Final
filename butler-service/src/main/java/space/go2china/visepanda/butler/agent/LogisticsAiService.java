package space.go2china.visepanda.butler.agent;

import dev.langchain4j.service.UserMessage;

public interface LogisticsAiService {
    @UserMessage("Return conservative China logistics guidance with cited sources only: {{task}}")
    AgentResult answer(String task);
}
