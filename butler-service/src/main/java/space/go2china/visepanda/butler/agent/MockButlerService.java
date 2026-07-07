package space.go2china.visepanda.butler.agent;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.model.AssistantResponse;
import space.go2china.visepanda.butler.model.BookingCandidate;
import space.go2china.visepanda.butler.model.ButlerAlert;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.Coordinates;
import space.go2china.visepanda.butler.model.TripBlock;
import space.go2china.visepanda.butler.model.TripDay;
import space.go2china.visepanda.butler.model.TripState;
import space.go2china.visepanda.butler.model.TripSummary;

@Service
public class MockButlerService {
    private static final Map<String, String> CITY_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, List<String>> CITY_HIGHLIGHTS = new LinkedHashMap<>();

    static {
        CITY_LOOKUP.put("beijing", "Beijing");
        CITY_LOOKUP.put("北京", "Beijing");
        CITY_LOOKUP.put("shanghai", "Shanghai");
        CITY_LOOKUP.put("上海", "Shanghai");
        CITY_LOOKUP.put("chengdu", "Chengdu");
        CITY_LOOKUP.put("成都", "Chengdu");
        CITY_LOOKUP.put("xi'an", "Xi'an");
        CITY_LOOKUP.put("xian", "Xi'an");
        CITY_LOOKUP.put("西安", "Xi'an");
        CITY_LOOKUP.put("guangzhou", "Guangzhou");
        CITY_LOOKUP.put("广州", "Guangzhou");
        CITY_LOOKUP.put("hangzhou", "Hangzhou");
        CITY_LOOKUP.put("杭州", "Hangzhou");
        CITY_LOOKUP.put("suzhou", "Suzhou");
        CITY_LOOKUP.put("苏州", "Suzhou");
        CITY_LOOKUP.put("chongqing", "Chongqing");
        CITY_LOOKUP.put("重庆", "Chongqing");
        CITY_LOOKUP.put("nanjing", "Nanjing");
        CITY_LOOKUP.put("南京", "Nanjing");
        CITY_LOOKUP.put("guilin", "Guilin");
        CITY_LOOKUP.put("桂林", "Guilin");
        CITY_LOOKUP.put("lijiang", "Lijiang");
        CITY_LOOKUP.put("丽江", "Lijiang");
        CITY_LOOKUP.put("yunnan", "Yunnan");
        CITY_LOOKUP.put("云南", "Yunnan");
        CITY_LOOKUP.put("shenzhen", "Shenzhen");
        CITY_LOOKUP.put("深圳", "Shenzhen");
        CITY_LOOKUP.put("hong kong", "Hong Kong");
        CITY_LOOKUP.put("香港", "Hong Kong");

        CITY_HIGHLIGHTS.put("Beijing", List.of("Forbidden City (故宫)", "Great Wall · Mutianyu (长城·慕田峪)", "Temple of Heaven (天坛)", "Summer Palace (颐和园)", "Hutong walk (胡同)"));
        CITY_HIGHLIGHTS.put("Shanghai", List.of("The Bund (外滩)", "Yu Garden (豫园)", "Nanjing Road (南京路)", "French Concession (法租界)", "Shanghai Tower (上海中心)"));
        CITY_HIGHLIGHTS.put("Chengdu", List.of("Giant Panda Base (大熊猫基地)", "Jinli Ancient Street (锦里)", "People's Park teahouse (人民公园)", "Wuhou Shrine (武侯祠)", "Kuanzhai Alley (宽窄巷子)"));
        CITY_HIGHLIGHTS.put("Xi'an", List.of("Terracotta Army (兵马俑)", "City Wall (西安城墙)", "Muslim Quarter (回民街)", "Big Wild Goose Pagoda (大雁塔)", "Bell Tower (钟楼)"));
        CITY_HIGHLIGHTS.put("Guangzhou", List.of("Canton Tower (广州塔)", "Shamian Island (沙面)", "Yuexiu Park (越秀公园)", "Beijing Road (北京路)", "Dim sum tea house"));
        CITY_HIGHLIGHTS.put("Hangzhou", List.of("West Lake (西湖)", "Lingyin Temple (灵隐寺)", "Longjing tea village (龙井)", "Hefang Street (河坊街)", "Grand Canal (大运河)"));
        CITY_HIGHLIGHTS.put("Suzhou", List.of("Humble Administrator's Garden (拙政园)", "Pingjiang Road (平江路)", "Tiger Hill (虎丘)", "Silk Museum (丝绸博物馆)", "Shantang Street (山塘街)"));
        CITY_HIGHLIGHTS.put("Chongqing", List.of("Hongya Cave (洪崖洞)", "Yangtze cable car (长江索道)", "Ciqikou old town (磁器口)", "Liziba monorail (李子坝)", "Hotpot dinner (火锅)"));
        CITY_HIGHLIGHTS.put("Guilin", List.of("Li River cruise (漓江)", "Reed Flute Cave (芦笛岩)", "Elephant Trunk Hill (象鼻山)", "Yangshuo West Street (阳朔西街)", "Longji rice terraces (龙脊梯田)"));
    }

    public CanvasPatch createPatch(String message, TripState current) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        List<ButlerAlert> alerts = alertsFor(normalized);

        if (includesAny(normalized, "first time", "first china trip", "5 days")) {
            return patch("create_trip",
                    "I drafted a first China trip with Beijing for cultural grounding and Shanghai for a smooth modern contrast.",
                    response("First China Trip", "Beijing gives you the classic cultural base, then Shanghai keeps the finish easy and modern.",
                            List.of("Balanced first-arrival pacing", "Key China setup reminders included"), "Book timed-entry sights before flights lock in."),
                    new TripSummary("First China Trip", 5, "Balanced", "First-time visitor", List.of("Beijing", "Shanghai"), "Draft"),
                    firstTripDays(), alerts.isEmpty() ? List.of(paymentAlert(), visaAlert()) : alerts,
                    "Created a first-time China itinerary across Beijing and Shanghai.");
        }

        List<String> cities = extractCities(normalized);
        int requestedDays = extractDayCount(normalized);
        boolean createIntent = includesAny(normalized, "plan", "create", "build", "design", "draft", "itinerary", "trip", "travel",
                "go to", "visit", "holiday", "vacation", "days", "day", "week", "规划", "计划", "行程", "天", "旅游", "路线", "去");
        List<String> destinations = current.summary() == null || current.summary().destinations() == null
                ? List.of() : current.summary().destinations();
        boolean hasNewDestination = cities.stream().anyMatch(city -> destinations.stream().noneMatch(dest -> dest.equalsIgnoreCase(city)));

        if (!cities.isEmpty() && createIntent && (requestedDays > 0 || hasNewDestination)) {
            int totalDays = requestedDays > 0 ? requestedDays : Math.min(10, Math.max(3, cities.size() * 2));
            String cityLabel = String.join(" + ", cities);
            return patch("create_trip",
                    "I drafted a " + totalDays + "-day " + cityLabel + " itinerary you can refine from here.",
                    response(cityLabel + " Trip", "This gives you a stable mock itinerary while live planning is unavailable.",
                            List.of(totalDays + " days", "Deterministic fallback", "Transit-friendly pacing"), "Confirm opening hours before departure."),
                    new TripSummary(cityLabel + " Trip", totalDays, "Balanced", travelerStyle(current), cities, "Draft"),
                    buildSkeletonDays(cities, totalDays), alerts.isEmpty() ? List.of(paymentAlert(), visaAlert()) : alerts,
                    "Created a " + totalDays + "-day itinerary for " + String.join(", ", cities) + ".");
        }

        if (includesAny(normalized, "less tiring", "slow", "slower", "relaxed", "lighter", "lighten", "calmer", "easier")) {
            return patch("adjust_trip", "I slowed the pace and kept the daily plan easier to recover from.",
                    response("Pace Slowed Down", "I trimmed each day to fewer moves so the canvas has more recovery space.",
                            List.of("Relaxed pace", "Fewer blocks per day"), null),
                    new TripSummary(null, null, "Relaxed", null, null, "Refined"), reviseDays(current, "relaxed"),
                    alerts, "Adjusted pace to Relaxed with fewer daily moves.");
        }

        if (includesAny(normalized, "budget", "cheap", "lower cost", "save money")) {
            return patch("adjust_trip", "I shifted the plan toward metro access, casual meals, and practical hotel areas.",
                    response("Budget Version", "I kept the route intact but biased it toward transit and casual food.",
                            List.of("Metro-first routing", "Casual meals"), null),
                    new TripSummary(null, null, null, null, null, "Refined"), reviseDays(current, "budget"),
                    alerts, "Adjusted budget assumptions toward lower-cost choices.");
        }

        if (includesAny(normalized, "food", "dining", "eat", "restaurant")) {
            return patch("adjust_trip", "I added more food-focused stops without making the route too dense.",
                    response("Food Emphasis Added", "I added dining notes to the existing canvas without changing the whole route.",
                            List.of("More local food", "Route stays stable"), null),
                    null, reviseDays(current, "food"), alerts, "Added dining emphasis to the canvas.");
        }

        if (includesAny(normalized, "hotel", "stay", "convenient")) {
            return patch("adjust_trip", "I moved hotel guidance toward convenient, transit-friendly areas.",
                    response("Stay Areas Updated", "Hotel guidance now favors central, transit-friendly neighborhoods.",
                            List.of("Central stay zones", "Lower transfer friction"), null),
                    null, reviseDays(current, "hotel"), alerts, "Updated hotel area guidance.");
        }

        String intent = alerts.isEmpty() ? "adjust_trip" : "add_alerts";
        return patch(intent,
                alerts.isEmpty() ? "I kept the current route and noted this as planning context." : "I added the relevant practical reminders to the canvas.",
                response(alerts.isEmpty() ? "Context Noted" : "Practical Reminders Added",
                        alerts.isEmpty() ? "No route change was needed yet." : "I added setup reminders without pretending to use live services.",
                        alerts.isEmpty() ? List.of("Canvas route unchanged") : List.of("Alerts added", "Mock fallback active"), null),
                new TripSummary(null, null, null, null, null, alerts.isEmpty() ? confidence(current) : "Refined"),
                null, alerts, alerts.isEmpty() ? "Added context without changing the route." : "Added practical butler reminders.");
    }

    private CanvasPatch patch(String intent, String assistantMessage, AssistantResponse response, TripSummary summary,
                              List<TripDay> days, List<ButlerAlert> alerts, String reason) {
        return new CanvasPatch(intent, assistantMessage, response, summary, days, alerts, reason);
    }

    private AssistantResponse response(String headline, String body, List<String> highlights, String watchOut) {
        return new AssistantResponse(headline, body, highlights, watchOut, "Tell me what you want to refine next.", null, null, "neutral");
    }

    private List<ButlerAlert> alertsFor(String normalized) {
        List<ButlerAlert> alerts = new ArrayList<>();
        if (includesAny(normalized, "payment", "alipay", "wechat pay", "card")) alerts.add(paymentAlert());
        if (includesAny(normalized, "visa", "entry", "passport", "transit")) alerts.add(visaAlert());
        if (includesAny(normalized, "translate", "translation", "language", "chinese")) alerts.add(languageAlert());
        if (includesAny(normalized, "emergency", "sos", "hospital", "passport lost")) alerts.add(emergencyAlert());
        return alerts;
    }

    private boolean includesAny(String message, String... words) {
        for (String word : words) {
            if (message.contains(word)) return true;
        }
        return false;
    }

    private ButlerAlert paymentAlert() {
        return new ButlerAlert("payment", "high", "Set up Alipay before arrival",
                "Payment setup prevents friction with taxis, restaurants, and small shops.", "Review payment setup", null);
    }

    private ButlerAlert visaAlert() {
        return new ButlerAlert("visa", "high", "Check entry rules before booking",
                "Visa-free and transit rules depend on nationality, city pair, and trip length.", "Review visa and entry checklist", null);
    }

    private ButlerAlert languageAlert() {
        return new ButlerAlert("language", "medium", "Prepare translation for taxis and dining",
                "Save hotel addresses and common food phrases in Chinese before you land.", "Open translation tools", null);
    }

    private ButlerAlert emergencyAlert() {
        return new ButlerAlert("emergency", "medium", "Save emergency contacts offline",
                "Keep embassy, hotel, passport, and insurance details available without roaming.", "Prepare emergency card", null);
    }

    private List<String> extractCities(String normalized) {
        List<String> found = new ArrayList<>();
        CITY_LOOKUP.forEach((alias, display) -> {
            if (normalized.contains(alias) && !found.contains(display)) found.add(display);
        });
        return found;
    }

    private int extractDayCount(String normalized) {
        Matcher weeks = Pattern.compile("(\\d+)\\s*(weeks?|周)").matcher(normalized);
        if (weeks.find()) return Math.min(21, Integer.parseInt(weeks.group(1)) * 7);
        Matcher days = Pattern.compile("(\\d+)\\s*(days?|nights?|天|晚)").matcher(normalized);
        if (days.find()) return Math.min(21, Integer.parseInt(days.group(1)));
        return 0;
    }

    private List<String> highlightsFor(String city) {
        return CITY_HIGHLIGHTS.getOrDefault(city, List.of(
                city + " historic center",
                city + " signature landmark",
                city + " local market & street food",
                city + " park or riverside walk",
                city + " evening night view"));
    }

    private List<TripDay> buildSkeletonDays(List<String> cities, int totalDays) {
        List<TripDay> days = new ArrayList<>();
        int perCity = Math.max(1, totalDays / cities.size());
        int dayNum = 1;
        int remaining = totalDays;
        for (int index = 0; index < cities.size(); index++) {
            String city = cities.get(index);
            int count = index == cities.size() - 1 ? remaining : Math.min(perCity, remaining);
            for (int i = 0; i < count; i++) {
                days.add(buildSkeletonDay(dayNum++, city, i));
                remaining--;
            }
        }
        int lastIndex = (int) days.stream().filter(day -> day.city().equals(cities.get(cities.size() - 1))).count();
        while (remaining > 0) {
            days.add(buildSkeletonDay(dayNum++, cities.get(cities.size() - 1), lastIndex++));
            remaining--;
        }
        return days;
    }

    private TripDay buildSkeletonDay(int dayNum, String city, int cityDayIndex) {
        List<String> h = highlightsFor(city);
        TripBlock morning = block("Morning", h.get((cityDayIndex * 3) % h.size()), "Start your " + city + " day at an easy pace.", city);
        TripBlock afternoon = block("Afternoon", h.get((cityDayIndex * 3 + 1) % h.size()), "Keep exploring " + city + " with manageable walking.", city);
        TripBlock evening = block("Evening", h.get((cityDayIndex * 3 + 2) % h.size()), "Wind down with dinner and an easy " + city + " evening.", city);
        return new TripDay(dayNum, city, "Balanced", List.of(morning, afternoon, evening),
                List.of(city + " local specialty", city + " street snack"),
                city + " central, transit-friendly area",
                cityDayIndex == 0 ? "Arrive in " + city + "; metro or taxi to the hotel." : "Metro and short rides within " + city + ".",
                "Reserve popular " + city + " sights ahead where booking is required.", "new");
    }

    private TripBlock block(String time, String title, String description, String city) {
        return new TripBlock(time, title, description, null, null, title + ", " + city,
                (title.contains("（") || title.contains("(")) ? title : null,
                null, "Confirm current hours before departure", "https://uri.amap.com/search?keyword=" + encode(title),
                null, null, "Static fallback", null);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private List<TripDay> firstTripDays() {
        return List.of(
                new TripDay(1, "Beijing", "Balanced", List.of(
                        block("Morning", "Arrival and check-in", "Stay near Wangfujing (王府井) or Dongcheng for convenient metro access.", "Beijing"),
                        block("Afternoon", "Temple of Heaven (天坛)", "Begin with a spacious, iconic site that is easier after a flight.", "Beijing"),
                        new TripBlock("Evening", "Easy hutong (胡同) dinner", "Choose a low-friction dinner close to the hotel.",
                                null, null, null, null, null, null, null, null, null, null, null)
                ), List.of("Hutong noodles", "Roast duck tasting"), "Wangfujing or Dongcheng",
                        "Short taxi rides on arrival day; metro when rested.", "Keep the first day light and practical.", "new"),
                new TripDay(2, "Beijing", "Balanced", List.of(
                        new TripBlock("Morning", "Forbidden City (故宫)", "Book ahead and enter early to avoid the busiest flow.",
                                null, null, "4 Jingshan Front Street, Dongcheng District, Beijing", "北京市东城区景山前街4号", null,
                                "Usually daytime entry; timed tickets required", "https://uri.amap.com/search?keyword=%E6%95%85%E5%AE%AB",
                                "https://intl.dpm.org.cn/", List.of(new BookingCandidate("static-ticket-forbidden-city", "ticket",
                                "Forbidden City official ticket info", "Official venue", "info-only",
                                "Information link only. Confirm current availability, passport rules, and refund policy before paying.", null, null)),
                                "Static fallback", new Coordinates(39.91635, 116.39715)),
                        new TripBlock("Afternoon", "Jingshan Park (景山公园) and hutongs (胡同)", "Pair one classic viewpoint with a slower neighborhood walk.",
                                null, null, null, null, null, null, null, null, null, null, null)
                ), List.of("Zhajiangmian", "Peking duck"), "Dongcheng", "Metro plus short rideshare hops.",
                        "Reserve timed tickets before arrival.", "new"),
                new TripDay(3, "Shanghai", "Balanced", List.of(
                        new TripBlock("Morning", "High-speed train to Shanghai (高铁)", "Use the train if you want city-center arrival and fewer airport steps.",
                                null, null, null, null, null, null, null, null, null, null, null),
                        block("Evening", "The Bund (外滩)", "Make the first Shanghai moment visually memorable but simple.", "Shanghai")
                ), List.of("Xiaolongbao", "Shanghainese noodles"), "People's Square or Jing'an",
                        "Train arrival plus metro or taxi to hotel.", "Avoid overpacking the transfer day.", "new")
        );
    }

    private List<TripDay> reviseDays(TripState current, String kind) {
        if (current.days() == null) return List.of();
        return current.days().stream().map(day -> switch (kind) {
            case "relaxed" -> new TripDay(day.day(), day.city(), "Relaxed",
                    day.blocks() == null ? List.of() : day.blocks().stream().limit(2).toList(),
                    day.food(), day.stay(), day.transport(), "This day is intentionally lighter to reduce fatigue.", "revised");
            case "budget" -> new TripDay(day.day(), day.city(), day.pace(), day.blocks(),
                    List.of("Casual local noodles", "Food court or neighborhood restaurant"),
                    day.stay(), "Prefer metro routes and short rides only when needed.",
                    "Budget version: keep hotels near transit and avoid unnecessary transfers.", "revised");
            case "food" -> new TripDay(day.day(), day.city(), day.pace(), day.blocks(),
                    append(day.food(), "Shanghai".equals(day.city()) ? "Xiaolongbao tasting" : "Regional snack stop"),
                    day.stay(), day.transport(), day.note(), "revised");
            case "hotel" -> new TripDay(day.day(), day.city(), day.pace(), day.blocks(), day.food(),
                    "Shanghai".equals(day.city()) ? "Jing'an or People's Square" : "Dongcheng or Wangfujing",
                    day.transport(), day.note(), "revised");
            default -> day;
        }).toList();
    }

    private List<String> append(List<String> input, String value) {
        List<String> copy = new ArrayList<>(input == null ? List.of() : input);
        copy.add(value);
        return copy;
    }

    private String travelerStyle(TripState current) {
        return current.summary() == null || current.summary().travelerStyle() == null
                ? "First-time visitor" : current.summary().travelerStyle();
    }

    private String confidence(TripState current) {
        return current.summary() == null || current.summary().confidence() == null ? "Draft" : current.summary().confidence();
    }
}
