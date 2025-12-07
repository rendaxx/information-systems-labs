package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.repository.view.OrderView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<OrderView> findViewById(Long id);
}
