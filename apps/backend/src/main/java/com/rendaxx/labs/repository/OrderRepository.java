package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Order;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Collection<OrderProj> findByMinTemperature(
            @NotNull
                    @Min(value = -100, message = "must not be lower than -100°C")
                    @Max(value = 200, message = "must not exceed 200°C")
                    Integer minTemperature);
}
