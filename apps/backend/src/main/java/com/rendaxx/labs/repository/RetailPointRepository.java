package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.repository.view.RetailPointView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RetailPointRepository extends JpaRepository<RetailPoint, Long>, JpaSpecificationExecutor<RetailPoint> {

    Optional<RetailPointView> findViewById(@Param("id") Long id);

    @Query(
            """
            select rp from RetailPoint rp
            where rp.id <> :retailPointId
            order by function('st_distance', (select origin.location from RetailPoint origin
            where origin.id = :retailPointId), rp.location), rp.id
            """)
    List<RetailPointView> findNearestRetailPointsView(@Param("retailPointId") Long retailPointId, Pageable pageable);
}
