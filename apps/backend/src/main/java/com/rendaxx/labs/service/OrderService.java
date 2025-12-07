package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.SaveOrderDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.OrderMapper;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.support.RepositoryGuard;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import java.util.Objects;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class OrderService {

    OrderMapper mapper;
    OrderRepository repository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;
    RepositoryGuard repositoryGuard;

    private static final String DESTINATION = "/topic/orders";

    public OrderDto create(SaveOrderDto command) {
        Order order = save(command, new Order());
        OrderDto dto = mapper.toDto(order);
        changePublisher.publish(DESTINATION, Objects.requireNonNull(order.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Order order = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id)));
        return mapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<Order> specification = specificationBuilder.build(filters);
        Page<Order> result = repositoryGuard.execute(() -> repository.findAll(specification, pageable));
        return result.map(mapper::toDto);
    }

    public OrderDto update(Long id, SaveOrderDto command) {
        Order order = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id)));
        Order savedOrder = save(command, order);
        OrderDto dto = mapper.toDto(savedOrder);
        changePublisher.publish(
                DESTINATION, Objects.requireNonNull(savedOrder.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Order order = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id)));
        repositoryGuard.execute(() -> repository.delete(order));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(order.getId()), null, EntityChangeType.DELETED);
    }

    private Order save(SaveOrderDto command, Order order) {
        mapper.update(order, command);
        return repositoryGuard.execute(() -> repository.save(order));
    }
}
