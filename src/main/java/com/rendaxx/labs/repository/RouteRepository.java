package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Route;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query(value = "select avg(r.mileage_in_km) from routes r", nativeQuery = true)
    BigDecimal findAverageMileageInKm();
}
