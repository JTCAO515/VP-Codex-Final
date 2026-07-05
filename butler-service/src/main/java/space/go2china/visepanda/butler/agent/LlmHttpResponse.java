package space.go2china.visepanda.butler.agent;

public record LlmHttpResponse(int status, String body) {
    public boolean ok() {
        return status >= 200 && status < 300;
    }
}
