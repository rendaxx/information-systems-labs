package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.RoutePoint;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long>, JpaSpecificationExecutor<RoutePoint> {
    @Query(
            """
            select ret from RetailPoint ret
            join RoutePoint rp on rp.retailPoint = ret
            group by ret.id
            order by count(rp.id) desc, ret.id asc""")
    List<RetailPoint> findMostVisitedRetailPoints(Pageable pageable);
}
