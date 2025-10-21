package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.RetailPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetailPointRepository extends JpaRepository<RetailPoint, Long> {

}
