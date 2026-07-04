package space.go2china.visepanda.butler.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(String url, String serviceRoleKey) {
    public boolean configured() {
        return url != null && !url.isBlank() && serviceRoleKey != null && !serviceRoleKey.isBlank();
    }
}
