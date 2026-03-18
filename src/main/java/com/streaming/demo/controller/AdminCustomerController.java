package com.streaming.demo.controller;

import com.streaming.demo.dto.CustomerDetailDto;
import com.streaming.demo.dto.MessageResponse;
import com.streaming.demo.service.AdminCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers")
//@PreAuthorize("hasRole('ADMIN' or 'CUSTOMER')")
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService adminCustomerService;

    @GetMapping
    public ResponseEntity<Page<CustomerDetailDto>> getAllCustomers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(adminCustomerService.getAllCustomers(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailDto> getCustomer(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminCustomerService.getCustomerById(id));
    }

    @PutMapping("/{id}/subscription")
    public ResponseEntity<?> updateSubscription(@PathVariable(name = "id") Long id, @RequestParam(name = "active") boolean active) {
        adminCustomerService.updateSubscriptionStatus(id, active);
        return ResponseEntity.ok(new MessageResponse("Subscription status updated successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable(name = "id") Long id, @RequestParam(name = "active") boolean active) {
        adminCustomerService.toggleCustomerStatus(id, active);
        return ResponseEntity.ok(new MessageResponse("Customer status updated successfully"));
    }
}
