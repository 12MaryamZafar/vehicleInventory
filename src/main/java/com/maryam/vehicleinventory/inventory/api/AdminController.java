package com.maryam.vehicleinventory.inventory.api;

import com.maryam.vehicleinventory.inventory.application.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DealerService dealerService;

    // GET /admin/dealers/countBySubscription
    // GLOBAL_ADMIN only — counts across ALL tenants
    @GetMapping("/dealers/countBySubscription")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<Map<String, Long>> countBySubscription() {
        return ResponseEntity.ok(dealerService.countBySubscription());
    }
}