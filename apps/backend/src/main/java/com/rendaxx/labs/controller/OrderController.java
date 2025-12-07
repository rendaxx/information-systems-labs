package com.rendaxx.labs.controller;

import com.rendaxx.labs.api.v1.api.OrdersApi;
import com.rendaxx.labs.api.v1.model.OrderApiDto;
import com.rendaxx.labs.api.v1.model.PageOrderApiDto;
import com.rendaxx.labs.api.v1.model.SaveOrderApiDto;
import com.rendaxx.labs.controller.support.FilterParameterMapper;
import com.rendaxx.labs.controller.support.PageRequestFactory;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.SaveOrderDto;
import com.rendaxx.labs.mappers.api.OrderApiMapper;
import com.rendaxx.labs.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class OrderController implements OrdersApi {

    OrderService orderService;
    OrderApiMapper orderApiMapper;
    PageRequestFactory pageRequestFactory;
    FilterParameterMapper filterParameterMapper;

    @Override
    public ResponseEntity<OrderApiDto> createOrder(@Valid SaveOrderApiDto saveOrderApiDto) {
        SaveOrderDto command = orderApiMapper.toDto(saveOrderApiDto);
        OrderDto created = orderService.create(command);
        OrderApiDto response = orderApiMapper.toApi(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<OrderApiDto> getOrder(@PathVariable("id") Long id) {
        OrderDto dto = orderService.getById(id);
        return ResponseEntity.ok(orderApiMapper.toApi(dto));
    }

    @Override
    public ResponseEntity<PageOrderApiDto> listOrders(
            @Nullable Integer page,
            @Nullable Integer size,
            @Nullable List<String> sort,
            @Nullable Map<String, String> filter) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Map<String, String> filters = filterParameterMapper.toFilters(filter != null ? filter : Map.of());
        Page<OrderDto> result = orderService.getAll(pageable, filters);
        PageOrderApiDto response = orderApiMapper.toOrderPage(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<OrderApiDto> updateOrder(
            @PathVariable("id") Long id, @Valid SaveOrderApiDto saveOrderApiDto) {
        SaveOrderDto command = orderApiMapper.toDto(saveOrderApiDto);
        OrderDto updated = orderService.update(id, command);
        return ResponseEntity.ok(orderApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
