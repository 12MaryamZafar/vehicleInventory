package com.maryam.vehicleinventory.inventory.api;

import com.maryam.vehicleinventory.inventory.application.VehicleService;
import com.maryam.vehicleinventory.inventory.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // POST /vehicles
    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicleService.create(request));
    }

    // GET /vehicles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    // GET /vehicles?model=&status=&priceMin=&priceMax=&subscription=&page=&sort=
    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getAll(
            @ModelAttribute VehicleFilterParams filters,
            @PageableDefault(size = 10, sort = "model") Pageable pageable) {
        return ResponseEntity.ok(vehicleService.getAll(filters, pageable));
    }

    // PATCH /vehicles/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    // DELETE /vehicles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}