package space.go2china.visepanda.butler.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LlmJsonRepair {
    private static final int MAX_BACKTRACK_STEPS = 24;
    private final ObjectMapper objectMapper;

    public LlmJsonRepair(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode parse(String raw) {
        String candidate = extract(raw);
        try {
            return objectMapper.readTree(candidate);
        } catch (Exception ignored) {
            // try repair below
        }

        String work = candidate;
        for (int i = 0; i < MAX_BACKTRACK_STEPS; i++) {
            try {
                return objectMapper.readTree(repair(work));
            } catch (Exception ignored) {
                int cut = Math.max(work.lastIndexOf(','), Math.max(work.lastIndexOf('{'), work.lastIndexOf('[')));
                if (cut <= 0) break;
                work = work.substring(0, cut);
            }
        }
        throw new LlmUnavailableException("Model output was not valid JSON even after truncation repair.");
    }

    String extract(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        java.util.regex.Matcher fence = java.util.regex.Pattern
                .compile("```(?:json)?\\s*([\\s\\S]*?)(?:```|$)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(trimmed);
        String body = fence.find() ? fence.group(1).trim() : trimmed;
        int start = body.indexOf('{');
        return start < 0 ? body : body.substring(start);
    }

    String repair(String input) {
        java.util.ArrayDeque<Character> stack = new java.util.ArrayDeque<>();
        boolean inString = false;
        boolean escaped = false;

        for (char ch : input.toCharArray()) {
            if (inString) {
                if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == '"') inString = false;
                continue;
            }
            if (ch == '"') inString = true;
            else if (ch == '{') stack.push('}');
            else if (ch == '[') stack.push(']');
            else if ((ch == '}' || ch == ']') && !stack.isEmpty() && stack.peek() == ch) stack.pop();
        }

        String repaired = input;
        if (escaped && !repaired.isEmpty()) repaired = repaired.substring(0, repaired.length() - 1);
        if (inString) repaired += "\"";
        repaired = repaired.replaceAll(":\\s*$", ":null");
        repaired = repaired.replaceAll(",\\s*\"(?:[^\"\\\\]|\\\\.)*\"\\s*$", "");
        repaired = repaired.replaceAll(",\\s*$", "");
        while (!stack.isEmpty()) repaired += stack.pop();
        return repaired;
    }
}
