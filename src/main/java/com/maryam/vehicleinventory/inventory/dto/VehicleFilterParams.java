package com.maryam.vehicleinventory.inventory.dto;

import com.maryam.vehicleinventory.inventory.enums.SubscriptionType;
import com.maryam.vehicleinventory.inventory.enums.VehicleStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleFilterParams {

    private String model;
    private VehicleStatus status;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private SubscriptionType subscription;
}