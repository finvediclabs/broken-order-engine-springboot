package com.trading.engine.controller;

import com.trading.engine.dto.OrderRequest;
import com.trading.engine.dto.OrderResponse;
import com.trading.engine.model.Order;
import com.trading.engine.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        // BUG #4: No input validation for negative quantities or prices
        OrderResponse response = orderService.placeOrder(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Order>> getOrdersBySymbol(@PathVariable String symbol) {
        List<Order> orders = orderService.getOrdersBySymbol(symbol);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/trader/{traderId}")
    public ResponseEntity<List<Order>> getOrdersByTrader(@PathVariable String traderId) {
        List<Order> orders = orderService.getOrdersByTrader(traderId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/active/{symbol}")
    public ResponseEntity<List<Order>> getActiveOrdersBySymbol(@PathVariable String symbol) {
        List<Order> orders = orderService.getActiveOrdersBySymbol(symbol);
        return ResponseEntity.ok(orders);
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 