package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.repository.view.DriverView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {

    Optional<DriverView> findViewById(Long id);
}
