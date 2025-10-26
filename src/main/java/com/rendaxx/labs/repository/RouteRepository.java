package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Route;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query(value = "select avg(r.mileage_in_km) from routes r", nativeQuery = true)
    BigDecimal findAverageMileageInKm();

    @Query("select distinct r from Route r where r.plannedStartTime >= :periodStart and r.plannedEndTime <= :periodEnd")
    List<Route> findWithinPeriodWithDetails(
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
}
