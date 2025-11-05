package com.rendaxx.labs.controller.support;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PageRequestFactory {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public Pageable build(Integer page, Integer size, List<String> sortValues) {
        int resolvedPage = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int resolvedSize = size != null && size > 0 ? size : DEFAULT_SIZE;
        Sort sort = resolveSort(sortValues);
        if (sort.isUnsorted()) {
            return PageRequest.of(resolvedPage, resolvedSize);
        }
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }

    private Sort resolveSort(List<String> sortValues) {
        if (CollectionUtils.isEmpty(sortValues)) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sortValues.stream().map(this::toOrder).toList();
        return Sort.by(orders);
    }

    private Sort.Order toOrder(String sortValue) {
        if (sortValue == null || sortValue.isBlank()) {
            return Sort.Order.asc("id");
        }
        String[] parts = sortValue.split(",");
        String property = parts[0];
        if (parts.length < 2) {
            return Sort.Order.asc(property);
        }
        try {
            return new Sort.Order(Sort.Direction.fromString(parts[1]), property);
        } catch (IllegalArgumentException ex) {
            return Sort.Order.asc(property);
        }
    }
}
