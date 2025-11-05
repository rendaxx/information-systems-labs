package com.rendaxx.labs.controller.support;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FilterParameterMapper {

    public Map<String, String> toFilters(Map<String, String> filter) {
        if (filter == null || filter.isEmpty()) {
            return Collections.emptyMap();
        }
        return filter.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getValue() != null
                        && !entry.getValue().isBlank())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
