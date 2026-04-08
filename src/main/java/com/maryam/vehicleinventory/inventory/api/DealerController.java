package com.maryam.vehicleinventory.inventory.api;

import com.maryam.vehicleinventory.inventory.application.DealerService;
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
@RequestMapping("/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

    // POST /dealers
    @PostMapping
    public ResponseEntity<DealerResponse> create(
            @Valid @RequestBody DealerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dealerService.create(request));
    }

    // GET /dealers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DealerResponse> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(dealerService.getById(id));
    }

    // GET /dealers?page=0&size=10&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<DealerResponse>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(dealerService.getAll(pageable));
    }

    // PATCH /dealers/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<DealerResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DealerUpdateRequest request) {
        return ResponseEntity.ok(dealerService.update(id, request));
    }

    // DELETE /dealers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        dealerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}