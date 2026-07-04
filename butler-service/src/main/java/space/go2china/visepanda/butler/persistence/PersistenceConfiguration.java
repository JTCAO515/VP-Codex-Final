package space.go2china.visepanda.butler.persistence;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SupabaseProperties.class)
public class PersistenceConfiguration {
}
