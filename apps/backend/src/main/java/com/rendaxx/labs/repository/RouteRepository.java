package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.repository.view.RouteView;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {

    @Query(
            """
            select distinct r from Route r
            left join fetch r.routePoints rp
            left join fetch rp.retailPoint
            left join fetch rp.orders
            left join fetch r.vehicle v
            left join fetch v.driver
            where r.id = :id
            """)
    Optional<RouteView> findViewById(@Param("id") Long id);

    @Query(value = "select avg(r.mileage_in_km) from routes r", nativeQuery = true)
    Optional<BigDecimal> findAverageMileageInKm();

    @Query(
            """
            select distinct r from Route r
            left join fetch r.routePoints rp
            left join fetch rp.retailPoint
            left join fetch rp.orders
            left join fetch r.vehicle v
            left join fetch v.driver
            where r.plannedStartTime >= :periodStart and r.plannedEndTime <= :periodEnd
            """)
    List<RouteView> findWithinPeriodWithDetailsView(
            @Param("periodStart") LocalDateTime periodStart, @Param("periodEnd") LocalDateTime periodEnd);

    @Query(
            """
            select distinct r from Route r
            left join fetch r.routePoints rp
            left join fetch rp.retailPoint
            left join fetch rp.orders
            left join fetch r.vehicle v
            left join fetch v.driver
            where exists (
                select 1 from RoutePoint rp2 where rp2.route = r and rp2.retailPoint.id = :retailPointId
            )
            """)
    List<RouteView> findAllByRetailPointIdView(@Param("retailPointId") Long retailPointId);
}
