package com.maryam.vehicleinventory.inventory.dto;

import com.maryam.vehicleinventory.inventory.enums.VehicleStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleUpdateRequest {

    // All fields optional for PATCH
    private String model;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private VehicleStatus status;
}