package com.rendaxx.labs.controller.support;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FilterParameterMapper {

    private static final Set<String> RESERVED = Set.of("page", "size", "sort");

    public Map<String, String> toFilters(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }

        return raw.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(normalizeKey(e.getKey()), e.getValue()))
                .filter(e -> StringUtils.hasText(e.getKey()) && StringUtils.hasText(e.getValue()))
                .filter(e -> !isReserved(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private String normalizeKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        String s = key.trim();

        if ("filter".equals(s)) {
            return null;
        }

        if (s.startsWith("filter[")) {
            int first = s.indexOf('[');
            int last = s.lastIndexOf(']');
            if (last > first) {
                String inner = s.substring(first + 1, last);
                inner = inner.replace("][", ".").trim();
                inner = inner.replaceAll("\\s*\\.\\s*", ".");
                return inner;
            }
        }

        if (s.startsWith("filter.")) {
            return s.substring("filter.".length());
        }

        return s;
    }

    private boolean isReserved(String key) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        if (RESERVED.contains(key)) {
            return true;
        }
        return key.startsWith("sort");
    }
}
