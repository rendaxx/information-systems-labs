package com.rendaxx.labs.mappers.api.support;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

public interface PageSortMapper {

    default List<String> toSortStrings(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return List.of();
        }
        return sort.stream()
                .map(order ->
                        order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.toList());
    }
}
