package space.go2china.visepanda.butler.agent;

import dev.langchain4j.service.UserMessage;

public interface TranslatorAiService {
    @UserMessage("Translate the traveler phrase and include any useful pronunciation note: {{task}}")
    AgentResult translate(String task);
}
