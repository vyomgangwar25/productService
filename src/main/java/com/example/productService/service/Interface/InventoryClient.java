package com.example.productService.service.Interface;

import com.example.productService.DTO.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", url = "http://localhost:8082")
public interface InventoryClient {
    @GetMapping("/inventory/check/{productId}")
    InventoryResponse checkQuantity(@PathVariable Integer productId, @RequestParam Integer productQuantity);
}
