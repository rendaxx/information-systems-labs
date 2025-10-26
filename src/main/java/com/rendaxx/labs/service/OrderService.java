package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.SaveOrderDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.OrderMapper;
import com.rendaxx.labs.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class OrderService {

    OrderMapper mapper;
    OrderRepository repository;
    EntityChangePublisher changePublisher;

    private static final String DESTINATION = "/topic/orders";

    public OrderDto create(SaveOrderDto command) {
        Order order = save(command, new Order());
        OrderDto dto = mapper.toDto(order);
        changePublisher.publish(DESTINATION, order.getId(), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Order order = repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id));
        return mapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    public OrderDto update(Long id, SaveOrderDto command) {
        Order order = repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id));
        Order savedOrder = save(command, order);
        OrderDto dto = mapper.toDto(savedOrder);
        changePublisher.publish(DESTINATION, savedOrder.getId(), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Order order = repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id));
        repository.delete(order);
        changePublisher.publish(DESTINATION, order.getId(), null, EntityChangeType.DELETED);
    }

    private Order save(SaveOrderDto command, Order order) {
        mapper.update(order, command);
        return repository.save(order);
    }
}
