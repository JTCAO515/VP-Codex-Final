package space.go2china.visepanda.butler.memory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PiiSanitizerTest {
    @Test
    void redactsEmailPhoneAndPassportBeforePersistence() {
        String sanitized = new PiiSanitizer().sanitize("Email me at traveler@example.com, phone +1 415-555-1212, passport E12345678.");

        assertThat(sanitized).contains("[redacted_email]", "[redacted_phone]", "[redacted_passport]");
        assertThat(sanitized).doesNotContain("traveler@example.com", "415-555-1212", "E12345678");
    }
}
