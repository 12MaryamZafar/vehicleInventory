package com.maryam.vehicleinventory.inventory.infrastructure;

import com.maryam.vehicleinventory.inventory.domain.Dealer;
import com.maryam.vehicleinventory.inventory.enums.SubscriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Map;
import java.util.UUID;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, UUID> {

    // Find dealer by id and tenantId — used for cross-tenant check
    Optional<Dealer> findByIdAndTenantId(UUID id, String tenantId);

    // Get all dealers for a tenant with pagination
    Page<Dealer> findAllByTenantId(String tenantId, Pageable pageable);

    // Check email uniqueness within a tenant
    boolean existsByEmailAndTenantId(String email, String tenantId);

    // Count dealers grouped by subscriptionType — GLOBAL, not tenant-scoped
    @Query("SELECT d.subscriptionType, COUNT(d) FROM Dealer d GROUP BY d.subscriptionType")
    java.util.List<Object[]> countBySubscriptionType();
}