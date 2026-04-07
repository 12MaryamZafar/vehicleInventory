package com.maryam.vehicleinventory.inventory.dto;

import com.maryam.vehicleinventory.inventory.enums.SubscriptionType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DealerResponse {

    private UUID id;
    private String tenantId;
    private String name;
    private String email;
    private SubscriptionType subscriptionType;
}