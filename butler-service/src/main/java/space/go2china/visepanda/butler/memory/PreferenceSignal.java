package space.go2china.visepanda.butler.memory;

public record PreferenceSignal(String key, String value, double confidence, String evidence, String source, boolean correction) {
}
