package com.rendaxx.labs.service.specification;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EqualitySpecificationBuilder {

    private static final Set<String> RESERVED_PARAMETERS = Set.of("page", "size", "sort");

    private final ConversionService conversionService;

    public <T> Specification<T> build(Map<String, String> rawFilters) {
        if (rawFilters == null || rawFilters.isEmpty()) {
            return null;
        }

        Map<String, String> filters = sanitize(rawFilters);
        if (filters.isEmpty()) {
            return null;
        }

        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                Path<?> path = resolvePath(root, entry.getKey());
                if (path == null) {
                    continue;
                }
                Object value = convertValue(entry.getValue(), path.getJavaType());
                if (value == null) {
                    continue;
                }
                predicates.add(criteriaBuilder.equal(path, value));
            }
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<String, String> sanitize(Map<String, String> filters) {
        if (filters.isEmpty()) {
            return Collections.emptyMap();
        }
        return filters.entrySet().stream()
                .filter(entry -> !isReserved(entry.getKey()))
                .filter(entry -> StringUtils.hasText(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, ignored) -> existing));
    }

    private boolean isReserved(String key) {
        if (key == null) {
            return true;
        }
        if (RESERVED_PARAMETERS.contains(key)) {
            return true;
        }
        return key.startsWith("sort");
    }

    private Path<?> resolvePath(Root<?> root, String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        String[] rawParts = key.split("\\.");
        List<String> parts = new ArrayList<>();
        for (String p : rawParts) {
            if (StringUtils.hasText(p) && p.endsWith("Id") && !p.equals("id")) {
                parts.add(p.substring(0, p.length() - 2));
                parts.add("id");
            } else {
                parts.add(p);
            }
        }

        Path<?> path = root;
        From<?, ?> currentFrom = root;
        Class<?> currentType = root.getJavaType();

        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                return null;
            }

            Field field = findField(currentType, part);
            if (field == null) {
                return null;
            }

            if (Collection.class.isAssignableFrom(field.getType())) {
                if (currentFrom == null) {
                    return null;
                }
                Join<?, ?> join = currentFrom.join(part, JoinType.LEFT);
                path = join;
                currentFrom = join;
                currentType = resolveCollectionElementType(field);
                continue;
            }

            try {
                path = path.get(part);
            } catch (IllegalArgumentException ex) {
                return null;
            }

            if (path instanceof From<?, ?> asFrom) {
                currentFrom = asFrom;
            } else {
                currentFrom = null;
            }
            currentType = field.getType();
        }
        return path;
    }

    private Object convertValue(String rawValue, Class<?> targetType) {
        if (!StringUtils.hasText(rawValue) || targetType == null) {
            return null;
        }
        if (targetType.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            try {
                return Enum.valueOf(enumType, rawValue);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        if (Objects.equals(targetType, String.class)) {
            return rawValue;
        }
        if (conversionService.canConvert(String.class, targetType)) {
            try {
                return conversionService.convert(rawValue, targetType);
            } catch (RuntimeException ex) {
                return null;
            }
        }
        return null;
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && !Objects.equals(current, Object.class)) {
            try {
                Field field = current.getDeclaredField(name);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Class<?> resolveCollectionElementType(Field field) {
        ResolvableType resolvableType = ResolvableType.forField(field);
        if (resolvableType.hasGenerics()) {
            Class<?> resolved = resolvableType.getGeneric(0).resolve();
            if (resolved != null) {
                return resolved;
            }
        }
        return Object.class;
    }
}
