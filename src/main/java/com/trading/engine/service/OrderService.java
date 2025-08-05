package com.trading.engine.service;

import com.trading.engine.dto.OrderRequest;
import com.trading.engine.dto.OrderResponse;
import com.trading.engine.model.Order;
import com.trading.engine.model.OrderStatus;
import com.trading.engine.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final MatchingEngineService matchingEngineService;
    
    public OrderResponse placeOrder(OrderRequest request) {
        try {
            // Create order from request
            Order order = createOrderFromRequest(request);
            
            // Process order through matching engine
            return matchingEngineService.processOrder(order);
            
        } catch (Exception e) {
            log.error("Error placing order: {}", e.getMessage(), e);
            OrderResponse response = new OrderResponse();
            response.setSuccess(false);
            response.setMessage("Error placing order: " + e.getMessage());
            return response;
        }
    }
    
    private Order createOrderFromRequest(OrderRequest request) {
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setSymbol(request.getSymbol().toUpperCase());
        order.setSide(request.getSide());
        order.setType(request.getType());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setTraderId(request.getTraderId());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
    
    private String generateOrderId() {
        return "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public OrderResponse getOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            OrderResponse response = new OrderResponse();
            response.setOrderId(order.getOrderId());
            response.setSymbol(order.getSymbol());
            response.setSide(order.getSide().name());
            response.setType(order.getType().name());
            response.setQuantity(order.getQuantity());
            response.setPrice(order.getPrice());
            response.setStatus(order.getStatus());
            response.setTraderId(order.getTraderId());
            response.setTimestamp(order.getTimestamp());
            response.setFilledQuantity(order.getFilledQuantity());
            response.setAveragePrice(order.getAveragePrice());
            response.setSuccess(true);
            response.setMessage("Order retrieved successfully");
            return response;
        } else {
            OrderResponse response = new OrderResponse();
            response.setSuccess(false);
            response.setMessage("Order not found");
            return response;
        }
    }
    
    public List<Order> getOrdersBySymbol(String symbol) {
        return orderRepository.findBySymbol(symbol);
    }
    
    public List<Order> getOrdersByTrader(String traderId) {
        return orderRepository.findByTraderId(traderId);
    }
    
    public List<Order> getActiveOrdersBySymbol(String symbol) {
        return orderRepository.findActiveOrdersBySymbol(symbol);
    }
    
    public OrderResponse cancelOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            if (order.getStatus() == OrderStatus.FILLED) {
                OrderResponse response = new OrderResponse();
                response.setSuccess(false);
                response.setMessage("Cannot cancel filled order");
                return response;
            }
            
            // BUG #3: Not removing order from in-memory order book when cancelling
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            
            OrderResponse response = new OrderResponse();
            response.setOrderId(order.getOrderId());
            response.setSymbol(order.getSymbol());
            response.setSide(order.getSide().name());
            response.setType(order.getType().name());
            response.setQuantity(order.getQuantity());
            response.setPrice(order.getPrice());
            response.setStatus(order.getStatus());
            response.setTraderId(order.getTraderId());
            response.setTimestamp(order.getTimestamp());
            response.setFilledQuantity(order.getFilledQuantity());
            response.setAveragePrice(order.getAveragePrice());
            response.setSuccess(true);
            response.setMessage("Order cancelled successfully");
            return response;
        } else {
            OrderResponse response = new OrderResponse();
            response.setSuccess(false);
            response.setMessage("Order not found");
            return response;
        }
    }
} 