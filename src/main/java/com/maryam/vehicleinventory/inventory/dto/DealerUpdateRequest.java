package com.maryam.vehicleinventory.inventory.dto;

import com.maryam.vehicleinventory.inventory.enums.SubscriptionType;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class DealerUpdateRequest {

    // All fields optional for PATCH
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    private SubscriptionType subscriptionType;
}