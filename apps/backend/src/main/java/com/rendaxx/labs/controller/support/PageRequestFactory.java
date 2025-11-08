package com.rendaxx.labs.controller.support;

import java.util.ArrayList;
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
        return sort.isUnsorted()
                ? PageRequest.of(resolvedPage, resolvedSize)
                : PageRequest.of(resolvedPage, resolvedSize, sort);
    }

    private static boolean isDir(String s) {
        return "asc".equalsIgnoreCase(s) || "desc".equalsIgnoreCase(s);
    }

    private Sort resolveSort(List<String> sortValues) {
        if (CollectionUtils.isEmpty(sortValues)) {
            return Sort.unsorted();
        }

        List<String> tokens = new ArrayList<>();
        for (String v : sortValues) {
            if (v == null) continue;
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
