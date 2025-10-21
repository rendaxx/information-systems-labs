package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.dtos.SaveOrderDto;
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

    public Order create(SaveOrderDto command) {
        return save(command, new Order());
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id));
    }

    @Transactional(readOnly = true)
    public List<Order> getAll() {
        return repository.findAll();
    }

    public Order update(Long id, SaveOrderDto command) {
        Order order = repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id));
        return save(command, order);
    }

    public void delete(Long id) {
        Order order = repository.findById(id).orElseThrow(() -> new NotFoundException(Order.class, id));
        repository.delete(order);
    }

    private Order save(SaveOrderDto command, Order order) {
        mapper.update(order, command);
        return repository.save(order);
    }
}
