package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.repository.view.RetailPointView;
import com.rendaxx.labs.repository.view.RoutePointView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long>, JpaSpecificationExecutor<RoutePoint> {

    @Query(
            """
            select rp from RoutePoint rp
            left join fetch rp.retailPoint
            left join fetch rp.orders
            where rp.id = :id
            """)
    Optional<RoutePointView> findViewById(@Param("id") Long id);

    @Query(
            """
            select ret from RetailPoint ret
            join RoutePoint rp on rp.retailPoint = ret
            group by ret.id
            order by count(rp.id) desc, ret.id asc""")
    List<RetailPointView> findMostVisitedRetailPointsView(Pageable pageable);
}
