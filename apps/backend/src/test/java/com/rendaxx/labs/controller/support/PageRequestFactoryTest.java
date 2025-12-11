package com.rendaxx.labs.controller.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageRequestFactoryTest {

    private final PageRequestFactory factory = new PageRequestFactory(0, 20, 1000);

    @Test
    void buildReturnsUnsortedWhenSortValuesAreNull() {
        Pageable pageable = factory.build(0, 10, null);

        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }

    @Test
    void buildReturnsUnsortedWhenTokensBecomeEmpty() {
        Pageable pageable = factory.build(0, 10, List.of("   ,   "));

        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }

    @Test
    void buildsAscendingOrderWhenOnlyPropertyProvided() {
        Pageable pageable = factory.build(0, 10, List.of("name"));

        Sort.Order order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void buildsDescendingOrderWhenDirectionIsProvided() {
        Pageable pageable = factory.build(0, 10, List.of("age", "desc"));

        Sort.Order order = pageable.getSort().getOrderFor("age");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void ignoresStandaloneDirectionsAndBuildsMultipleOrders() {
        Pageable pageable = factory.build(1, 5, List.of("asc", "title,desc,createdAt"));

        List<Sort.Order> orders = new ArrayList<>();
        pageable.getSort().forEach(orders::add);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("title");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1).getProperty()).isEqualTo("createdAt");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
