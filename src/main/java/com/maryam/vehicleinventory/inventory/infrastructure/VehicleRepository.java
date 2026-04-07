package com.maryam.vehicleinventory.inventory.infrastructure;

import com.maryam.vehicleinventory.inventory.domain.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID>,
        JpaSpecificationExecutor<Vehicle> {

    // Find vehicle by id and tenantId — cross-tenant check
    Optional<Vehicle> findByIdAndTenantId(UUID id, String tenantId);

    // Check if dealer belongs to tenant before adding vehicle
    boolean existsByDealerIdAndTenantId(UUID dealerId, String tenantId);
}