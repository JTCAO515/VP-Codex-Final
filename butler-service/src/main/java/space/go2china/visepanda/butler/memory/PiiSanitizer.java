package space.go2china.visepanda.butler.memory;

import org.springframework.stereotype.Component;

@Component
public class PiiSanitizer {
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return input
                .replaceAll("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", "[redacted_email]")
                .replaceAll("\\b(?:\\+?\\d[\\d\\s().-]{7,}\\d)\\b", "[redacted_phone]")
                .replaceAll("(?i)\\b(?:passport|护照)\\s*(?:no\\.?|number|号码)?\\s*[:#：]?\\s*[A-Z0-9]{6,12}\\b", "[redacted_passport]")
                .replaceAll("(?i)\\b[A-Z][0-9]{7,9}\\b", "[redacted_passport]");
    }
}
