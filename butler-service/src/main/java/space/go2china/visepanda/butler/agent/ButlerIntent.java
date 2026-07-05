package space.go2china.visepanda.butler.agent;

public enum ButlerIntent {
    CREATE_TRIP("create_trip"),
    ADJUST_TRIP("adjust_trip"),
    ADD_LOCATION("add_location"),
    ADD_POI("add_poi"),
    ASK_RECOMMENDATION("ask_recommendation"),
    ASK_FACTUAL("ask_factual"),
    PREFERENCE_SIGNAL("preference_signal"),
    CONCERN("concern"),
    LOGISTICS("logistics"),
    UNCLEAR("unclear");

    private final String wireName;

    ButlerIntent(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }
}
