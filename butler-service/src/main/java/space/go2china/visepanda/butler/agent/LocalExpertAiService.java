package space.go2china.visepanda.butler.agent;

import dev.langchain4j.service.UserMessage;

public interface LocalExpertAiService {
    @UserMessage("Return local China travel guidance with cited sources only: {{task}}")
    AgentResult answer(String task);
}
