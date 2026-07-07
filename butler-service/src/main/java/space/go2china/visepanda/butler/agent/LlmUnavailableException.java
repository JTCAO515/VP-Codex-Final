package space.go2china.visepanda.butler.agent;

public class LlmUnavailableException extends RuntimeException {
    public LlmUnavailableException(String message) {
        super(message);
    }
}
