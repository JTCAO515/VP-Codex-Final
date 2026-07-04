package space.go2china.visepanda.butler.model;

import java.util.List;

public record TripDay(
        int day,
        String city,
        String pace,
        List<TripBlock> blocks,
        List<String> food,
        String stay,
        String transport,
        String note,
        String status
) {
}
