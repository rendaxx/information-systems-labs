package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.RetailPoint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RetailPointRepository extends JpaRepository<RetailPoint, Long> {

    @Query(value = "SELECT rp.* FROM retail_points rp "
            + "JOIN retail_points origin ON origin.id = :retailPointId "
            + "WHERE rp.id <> origin.id "
            + "ORDER BY origin.location <-> rp.location, rp.id "
            + "LIMIT :limit", nativeQuery = true)
    List<RetailPoint> findNearestRetailPoints(
        @Param("retailPointId") Long retailPointId,
        @Param("limit") int limit
    );
}
