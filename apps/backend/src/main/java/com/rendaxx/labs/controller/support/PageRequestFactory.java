package com.rendaxx.labs.controller.support;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PageRequestFactory {
    private final int defaultPage;
    private final int defaultSize;
    private final int maxSize;

    public PageRequestFactory(
            @Value("${labs.paging.default-page:0}") int defaultPage,
            @Value("${labs.paging.default-size:20}") int defaultSize,
            @Value("${labs.paging.max-size:1000}") int maxSize) {
        this.defaultPage = defaultPage;
        this.defaultSize = defaultSize;
        this.maxSize = maxSize;
    }

    public Pageable build(@Nullable Integer page, @Nullable Integer size, @Nullable List<String> sortValues) {
        int resolvedSize = size != null && size > 0 ? size : defaultSize;
        long requestedPage = page != null && page >= 0 ? page.longValue() : defaultPage;
        int resolvedPage = Math.toIntExact(Math.min(requestedPage, maxSize));
        Sort sort = resolveSort(sortValues);
        return sort.isUnsorted()
                ? PageRequest.of(resolvedPage, resolvedSize)
                : PageRequest.of(resolvedPage, resolvedSize, sort);
    }

    private static boolean isDir(String s) {
        return "asc".equalsIgnoreCase(s) || "desc".equalsIgnoreCase(s);
    }

    private Sort resolveSort(@Nullable List<String> sortValues) {
        if (CollectionUtils.isEmpty(sortValues)) {
            return Sort.unsorted();
        }

        List<String> tokens = new ArrayList<>();
        for (String v : sortValues) {
            for (String p : v.split(",")) {
                String t = p.trim();
                if (!t.isEmpty()) {
                    tokens.add(t);
                }
            }
        }
        if (tokens.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (int i = 0; i < tokens.size(); ) {
            String property = tokens.get(i);
            if (isDir(property)) {
                i++;
                continue;
            }
            Sort.Direction dir = Sort.Direction.ASC;
            if (i + 1 < tokens.size() && isDir(tokens.get(i + 1))) {
                dir = Sort.Direction.fromString(tokens.get(i + 1));
                i += 2;
            } else {
                i += 1;
            }
            if (!property.isBlank()) {
                orders.add(new Sort.Order(dir, property));
            }
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
