package space.go2china.visepanda.butler.agent;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RouterAgent {
    private record Rule(ButlerIntent intent, List<Pattern> patterns) {
    }

    private static Pattern pattern(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    // Order matches lib/ai/intentClassifier.ts: earlier rules win.
    private static final List<Rule> RULES = List.of(
            new Rule(ButlerIntent.ASK_FACTUAL, List.of(
                    pattern("\\b(visa|passport|entry|customs|transit)\\b"),
                    pattern("\\b(do i need|am i allowed|requirement|how (do|can) i pay|alipay|wechat pay)\\b"),
                    pattern("\\b(esim|sim card|vpn|exchange rate|currency|metro card|how much (is|does))\\b")
            )),
            new Rule(ButlerIntent.CONCERN, List.of(
                    pattern("\\b(is it safe|safety|dangerous|risk|scam(med)?|emergency|worried|afraid|hospital)\\b"),
                    pattern("\\b(robbed|stolen|theft|pickpocket|lost my (passport|wallet|phone)|injured|hurt|urgent help|call (the )?police|ambulance)\\b")
            )),
            new Rule(ButlerIntent.LOGISTICS, List.of(
                    pattern("\\b(how (do|to) (i )?get from|train|high[- ]?speed rail|flight|transfer|between)\\b.*\\bto\\b"),
                    pattern("\\b(metro|subway|route|directions|how long (does|to)|distance)\\b")
            )),
            new Rule(ButlerIntent.ASK_RECOMMENDATION, List.of(
                    pattern("\\b(best|top|recommend|where (should|can) i|what.s good|any good|must[- ]?(eat|see|try|visit))\\b"),
                    pattern("\\b(suggest|famous for|worth (visiting|seeing))\\b")
            )),
            new Rule(ButlerIntent.PREFERENCE_SIGNAL, List.of(
                    pattern("\\b(i (love|like|prefer|hate|don.t (eat|like|want)|can.t (eat|walk))|i.m (a )?(vegetarian|vegan|foodie))\\b"),
                    pattern("\\b(budget|student|luxury|cheap|allergic|no pork|no seafood|halal|with (kids|my family|toddlers))\\b"),
                    pattern("\\b(tired|exhausted|slow(er)? pace|too much walking)\\b")
            )),
            new Rule(ButlerIntent.ADD_POI, List.of(
                    pattern("\\b(add|include|put in|insert)\\b.*\\b(to (my|the) (trip|day|itinerary|canvas))\\b"),
                    pattern("\\badd\\b.+\\bto my trip\\b")
            )),
            new Rule(ButlerIntent.ADD_LOCATION, List.of(
                    pattern("\\b(add|include|also visit|squeeze in|stop in)\\b.*\\b(a (day|night)|city|to the route)\\b")
            )),
            new Rule(ButlerIntent.ADJUST_TRIP, List.of(
                    pattern("\\b(make (it|this|the|day)|change|adjust|rebalance|swap|replace|less|more|lighten|shorten|extend)\\b"),
                    pattern("\\b(too (packed|busy|tiring|much)|slower|faster|easier)\\b")
            )),
            new Rule(ButlerIntent.CREATE_TRIP, List.of(
                    pattern("\\b(plan|build|create|design|draft|make me)\\b.*\\b(trip|itinerary|days?|route|vacation|holiday)\\b"),
                    pattern("\\b(first (time|trip)|\\d+\\s*(day|days|week|weeks)\\b.*\\b(china|beijing|shanghai|trip))\\b"),
                    pattern("\\b(i (want|would like) to (go|travel|visit))\\b")
            ))
    );

    public ButlerIntent classify(String message) {
        String normalized = message == null ? "" : message.toLowerCase().trim();
        if (normalized.isEmpty()) return ButlerIntent.UNCLEAR;

        return RULES.stream()
                .filter(rule -> rule.patterns().stream().anyMatch(pattern -> pattern.matcher(normalized).find()))
                .map(Rule::intent)
                .findFirst()
                .orElse(ButlerIntent.UNCLEAR);
    }
}
