package com.rendaxx.labs.repository;

import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.repository.view.VehicleView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    Optional<VehicleView> findViewById(Long id);
}
