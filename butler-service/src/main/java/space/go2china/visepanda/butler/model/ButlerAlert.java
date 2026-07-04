package space.go2china.visepanda.butler.model;

public record ButlerAlert(
        String type,
        String priority,
        String title,
        String body,
        String action,
        Boolean done
) {
}
