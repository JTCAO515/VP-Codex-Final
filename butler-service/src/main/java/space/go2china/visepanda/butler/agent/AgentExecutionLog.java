package space.go2china.visepanda.butler.agent;

public record AgentExecutionLog(String agent, String task, long elapsedMs, boolean partial, int toolCalls) {
}
