package space.go2china.visepanda.butler.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PreferenceSignalExtractor {
    private static final Pattern VEGETARIAN_CORRECTION = Pattern.compile("\\b(now )?(not vegetarian|no longer vegetarian)\\b|不吃素了|不是素食");

    public List<PreferenceSignal> extract(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        List<PreferenceSignal> signals = new ArrayList<>();

        if (VEGETARIAN_CORRECTION.matcher(normalized).find()) {
            signals.add(signal("dietary", "vegetarian", 0.0, message, "explicit", true));
            return signals;
        }

        if (matches(normalized, "\\b(i am|i'm|i m|we are|we're|we re) (a )?(vegetarian|vegan)\\b|我吃素|不吃肉")) signals.add(signal("dietary", "vegetarian", 0.9, message, "explicit", false));
        if (matches(normalized, "\\b(no pork|halal|muslim)\\b|清真|不吃猪肉")) signals.add(signal("dietary", "no pork / halal-aware", 0.9, message, "explicit", false));
        if (matches(normalized, "\\b(no seafood|seafood allergy|allergic to seafood)\\b|海鲜过敏|不吃海鲜")) signals.add(signal("dietary", "no seafood", 0.9, message, "explicit", false));
        if (matches(normalized, "\\b(vegetarian restaurant|vegan restaurant|vegetarian food)\\b|素食餐厅|素食")) signals.add(signal("dietary", "vegetarian", 0.35, message, "inferred", false));

        if (matches(normalized, "\\b(less tiring|slow|slower|easy|light pace|too much walking|can't walk|cannot walk)\\b")) signals.add(signal("pace", "light", 0.85, message, "explicit", false));
        if (matches(normalized, "\\b(packed|as much as possible|full schedule)\\b")) signals.add(signal("pace", "packed", 0.85, message, "explicit", false));
        if (matches(normalized, "\\b(balanced|not too rushed)\\b")) signals.add(signal("pace", "balanced", 0.85, message, "explicit", false));

        if (matches(normalized, "\\b(student|cheap|budget|save money|lower cost|economy)\\b")) signals.add(signal("budget", "economy", 0.8, message, "explicit", false));
        if (matches(normalized, "\\b(mid[- ]?range|moderate|comfortable)\\b")) signals.add(signal("budget", "mid", 0.8, message, "explicit", false));
        if (matches(normalized, "\\b(luxury|five star|5 star|premium|high end)\\b")) signals.add(signal("budget", "luxury", 0.8, message, "explicit", false));

        if (matches(normalized, "\\b(with kids|with my family|family|children|toddler)\\b")) signals.add(signal("party", "family_with_kids", 0.8, message, "explicit", false));
        if (matches(normalized, "\\b(couple|honeymoon|partner|wife|husband|girlfriend|boyfriend)\\b")) signals.add(signal("party", "couple", 0.8, message, "explicit", false));
        if (matches(normalized, "\\b(group|friends)\\b")) signals.add(signal("party", "group", 0.8, message, "explicit", false));
        if (matches(normalized, "\\b(solo|alone|by myself)\\b")) signals.add(signal("party", "solo", 0.8, message, "explicit", false));

        if (matches(normalized, "\\b(history|museum|temple|ancient|culture)\\b")) signals.add(signal("interest", "history and culture", 0.55, message, "inferred", false));
        if (matches(normalized, "\\b(nature|mountain|lake|park|scenery)\\b")) signals.add(signal("interest", "nature", 0.55, message, "inferred", false));
        if (matches(normalized, "\\b(food|restaurant|eat|cuisine|market|hotpot|sichuan|street food|foodie|local food)\\b")) signals.add(signal("interest", "food", 0.55, message, "inferred", false));
        if (matches(normalized, "\\b(shopping|mall|market|souvenir)\\b")) signals.add(signal("interest", "shopping", 0.55, message, "inferred", false));

        return signals;
    }

    private boolean matches(String value, String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(value).find();
    }

    private PreferenceSignal signal(String key, String value, double confidence, String evidence, String source, boolean correction) {
        return new PreferenceSignal(key, value, confidence, evidence, source, correction);
    }
}
