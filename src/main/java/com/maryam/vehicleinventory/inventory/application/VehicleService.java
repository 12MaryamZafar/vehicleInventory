package com.maryam.vehicleinventory.inventory.application;

import com.maryam.vehicleinventory.inventory.domain.Dealer;
import com.maryam.vehicleinventory.inventory.domain.Vehicle;
import com.maryam.vehicleinventory.inventory.dto.*;
import com.maryam.vehicleinventory.inventory.exception.ResourceNotFoundException;
import com.maryam.vehicleinventory.inventory.exception.TenantMismatchException;
import com.maryam.vehicleinventory.inventory.infrastructure.DealerRepository;
import com.maryam.vehicleinventory.inventory.infrastructure.VehicleRepository;
import com.maryam.vehicleinventory.inventory.infrastructure.VehicleSpecification;
import com.maryam.vehicleinventory.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;

    // ─── CREATE ───────────────────────────────────────────────
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        // Dealer must exist and belong to same tenant
        Dealer dealer = dealerRepository
                .findByIdAndTenantId(request.getDealerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + request.getDealerId() +
                                " in current tenant"
                ));

        Vehicle vehicle = Vehicle.builder()
                .tenantId(tenantId)
                .dealer(dealer)
                .model(request.getModel())
                .price(request.getPrice())
                .status(request.getStatus())
                .build();

        return toResponse(vehicleRepository.save(vehicle));
    }

    // ─── GET BY ID ────────────────────────────────────────────
    public VehicleResponse getById(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        Vehicle vehicle = findByIdAndValidateTenant(id, tenantId);
        return toResponse(vehicle);
    }

    // ─── GET ALL WITH FILTERS (paginated) ─────────────────────
    public Page<VehicleResponse> getAll(VehicleFilterParams filters,
                                        Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();

        Specification<Vehicle> spec = VehicleSpecification.buildFilter(
                tenantId,
                filters.getModel(),
                filters.getStatus(),
                filters.getPriceMin(),
                filters.getPriceMax(),
                filters.getSubscription()
        );

        return vehicleRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    // ─── UPDATE (PATCH) ───────────────────────────────────────
    @Transactional
    public VehicleResponse update(UUID id, VehicleUpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        Vehicle vehicle = findByIdAndValidateTenant(id, tenantId);

        if (request.getModel() != null && !request.getModel().isBlank()) {
            vehicle.setModel(request.getModel());
        }
        if (request.getPrice() != null) {
            vehicle.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }

        return toResponse(vehicleRepository.save(vehicle));
    }

    // ─── DELETE ───────────────────────────────────────────────
    @Transactional
    public void delete(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        Vehicle vehicle = findByIdAndValidateTenant(id, tenantId);
        vehicleRepository.delete(vehicle);
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────
    private Vehicle findByIdAndValidateTenant(UUID id, String tenantId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with id: " + id
                ));

        if (!vehicle.getTenantId().equals(tenantId)) {
            throw new TenantMismatchException(
                    "Access denied to resource belonging to different tenant"
            );
        }

        return vehicle;
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .tenantId(vehicle.getTenantId())
                .dealerId(vehicle.getDealer().getId())
                .dealerName(vehicle.getDealer().getName())
                .model(vehicle.getModel())
                .price(vehicle.getPrice())
                .status(vehicle.getStatus())
                .build();
    }
}