package com.maryam.vehicleinventory.inventory.application;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.maryam.vehicleinventory.inventory.domain.Dealer;
import com.maryam.vehicleinventory.inventory.dto.*;
import com.maryam.vehicleinventory.inventory.exception.DuplicateResourceException;
import com.maryam.vehicleinventory.inventory.exception.ResourceNotFoundException;
import com.maryam.vehicleinventory.inventory.exception.TenantMismatchException;
import com.maryam.vehicleinventory.inventory.infrastructure.DealerRepository;
import com.maryam.vehicleinventory.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;

    // ─── CREATE ───────────────────────────────────────────────
    @Transactional
    public DealerResponse create(DealerRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        if (dealerRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new DuplicateResourceException(
                    "Dealer with email " + request.getEmail() +
                            " already exists in this tenant"
            );
        }

        Dealer dealer = Dealer.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .subscriptionType(request.getSubscriptionType())
                .build();

        return toResponse(dealerRepository.save(dealer));
    }

    // ─── GET BY ID ────────────────────────────────────────────
    public DealerResponse getById(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        Dealer dealer = findByIdAndValidateTenant(id, tenantId);
        return toResponse(dealer);
    }

    // ─── GET ALL (paginated) ──────────────────────────────────
    public Page<DealerResponse> getAll(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return dealerRepository
                .findAllByTenantId(tenantId, pageable)
                .map(this::toResponse);
    }

    // ─── UPDATE (PATCH) ───────────────────────────────────────
    @Transactional
    public DealerResponse update(UUID id, DealerUpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        Dealer dealer = findByIdAndValidateTenant(id, tenantId);

        if (request.getName() != null && !request.getName().isBlank()) {
            dealer.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(dealer.getEmail()) &&
                    dealerRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                throw new DuplicateResourceException(
                        "Dealer with email " + request.getEmail() +
                                " already exists in this tenant"
                );
            }
            dealer.setEmail(request.getEmail());
        }
        if (request.getSubscriptionType() != null) {
            dealer.setSubscriptionType(request.getSubscriptionType());
        }

        return toResponse(dealerRepository.save(dealer));
    }

    // ─── DELETE ───────────────────────────────────────────────
    @Transactional
    public void delete(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        Dealer dealer = findByIdAndValidateTenant(id, tenantId);
        dealerRepository.delete(dealer);
    }

    // ─── ADMIN: count by subscription (global) ────────────────
    public Map<String, Long> countBySubscription() {
        List<Object[]> results = dealerRepository.countBySubscriptionType();
        Map<String, Long> counts = new HashMap<>();
        counts.put("BASIC", 0L);
        counts.put("PREMIUM", 0L);
        for (Object[] row : results) {
            counts.put(row[0].toString(), (Long) row[1]);
        }
        return counts;
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────
    private Dealer findByIdAndValidateTenant(UUID id, String tenantId) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + id
                ));

        if (!dealer.getTenantId().equals(tenantId)) {
            throw new TenantMismatchException(
                    "Access denied to resource belonging to different tenant"
            );
        }

        return dealer;
    }

    private DealerResponse toResponse(Dealer dealer) {
        return DealerResponse.builder()
                .id(dealer.getId())
                .tenantId(dealer.getTenantId())
                .name(dealer.getName())
                .email(dealer.getEmail())
                .subscriptionType(dealer.getSubscriptionType())
                .build();
    }
}