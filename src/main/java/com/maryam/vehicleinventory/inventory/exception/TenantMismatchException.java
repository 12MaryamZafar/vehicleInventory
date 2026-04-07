package com.maryam.vehicleinventory.inventory.exception;

public class TenantMismatchException extends RuntimeException {

    public TenantMismatchException(String message) {
        super(message);
    }
}