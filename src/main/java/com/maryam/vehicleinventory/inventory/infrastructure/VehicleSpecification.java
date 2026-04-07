package com.maryam.vehicleinventory.inventory.infrastructure;

import com.maryam.vehicleinventory.inventory.domain.Dealer;
import com.maryam.vehicleinventory.inventory.domain.Vehicle;
import com.maryam.vehicleinventory.inventory.enums.SubscriptionType;
import com.maryam.vehicleinventory.inventory.enums.VehicleStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VehicleSpecification {

    // Tenant scope — always applied
    public static Specification<Vehicle> hasTenant(String tenantId) {
        return (root, query, cb) ->
                cb.equal(root.get("tenantId"), tenantId);
    }

    // Filter by model (case-insensitive contains)
    public static Specification<Vehicle> hasModel(String model) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("model")),
                        "%" + model.toLowerCase() + "%");
    }

    // Filter by status
    public static Specification<Vehicle> hasStatus(VehicleStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    // Filter by minimum price
    public static Specification<Vehicle> hasPriceMin(BigDecimal priceMin) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), priceMin);
    }

    // Filter by maximum price
    public static Specification<Vehicle> hasPriceMax(BigDecimal priceMax) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), priceMax);
    }

    // Filter by dealer subscription type — JOINs to dealer table
    public static Specification<Vehicle> hasDealerSubscription(SubscriptionType subscriptionType) {
        return (root, query, cb) -> {
            Join<Vehicle, Dealer> dealerJoin = root.join("dealer", JoinType.INNER);
            return cb.equal(dealerJoin.get("subscriptionType"), subscriptionType);
        };
    }

    // Combine all filters dynamically
    public static Specification<Vehicle> buildFilter(
            String tenantId,
            String model,
            VehicleStatus status,
            BigDecimal priceMin,
            BigDecimal priceMax,
            SubscriptionType subscription) {

        List<Specification<Vehicle>> specs = new ArrayList<>();

        // Tenant is always required
        specs.add(hasTenant(tenantId));

        if (model != null && !model.isBlank()) {
            specs.add(hasModel(model));
        }
        if (status != null) {
            specs.add(hasStatus(status));
        }
        if (priceMin != null) {
            specs.add(hasPriceMin(priceMin));
        }
        if (priceMax != null) {
            specs.add(hasPriceMax(priceMax));
        }
        if (subscription != null) {
            specs.add(hasDealerSubscription(subscription));
        }

        return specs.stream()
                .reduce(Specification.where(null), Specification::and);
    }
}