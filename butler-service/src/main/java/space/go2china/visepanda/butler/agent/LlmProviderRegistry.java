package space.go2china.visepanda.butler.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class LlmProviderRegistry {
    private final Environment env;

    public LlmProviderRegistry(Environment env) {
        this.env = env;
    }

    public List<LlmProvider> configuredProviders() {
        List<LlmProvider> providers = new ArrayList<>();
        add(providers, "qwen", "Qwen (Aliyun Bailian)", key("DASHSCOPE_API_KEY", "ALIYUN_BAILIAN_API_KEY"),
                value("DASHSCOPE_COMPATIBLE_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1"),
                value("QWEN_CHAT_MODEL", "qwen3.6-flash"), Map.of("enable_thinking", false));
        add(providers, "zhipu", "Zhipu GLM", key("ZHIPU_API_KEY", "GLM_API_KEY"),
                value("ZHIPU_BASE_URL", "https://open.bigmodel.cn/api/paas/v4"),
                value("ZHIPU_CHAT_MODEL", "glm-5.1"), Map.of("thinking", Map.of("type", "disabled")));
        add(providers, "moonshot", "Moonshot Kimi", key("MOONSHOT_API_KEY", "KIMI_API_KEY"),
                value("MOONSHOT_BASE_URL", "https://api.moonshot.cn/v1"),
                value("MOONSHOT_CHAT_MODEL", "kimi-k2.6"), Map.of("temperature", 1));
        return providers;
    }

    private void add(List<LlmProvider> providers, String id, String label, String apiKey, String baseUrl, String model,
                     Map<String, Object> extraBody) {
        if (apiKey == null || apiKey.isBlank()) return;
        providers.add(new LlmProvider(id, label, apiKey, stripSlash(baseUrl), model, extraBody));
    }

    private String key(String primary, String alias) {
        String value = env.getProperty(primary);
        if (value != null && !value.isBlank()) return value.trim();
        value = env.getProperty(alias);
        return value == null ? "" : value.trim();
    }

    private String value(String name, String fallback) {
        String value = env.getProperty(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String stripSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
