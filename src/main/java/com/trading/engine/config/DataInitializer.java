package com.trading.engine.config;

import com.trading.engine.dto.OrderRequest;
import com.trading.engine.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OrderService orderService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing sample data...");
        
        // Create sample buy orders
        createSampleOrder("AAPL", "BUY", "LIMIT", new BigDecimal("100"), new BigDecimal("150.00"), "TRADER001");
        createSampleOrder("AAPL", "BUY", "LIMIT", new BigDecimal("50"), new BigDecimal("149.50"), "TRADER002");
        createSampleOrder("AAPL", "BUY", "LIMIT", new BigDecimal("75"), new BigDecimal("149.00"), "TRADER003");
        
        // Create sample sell orders
        createSampleOrder("AAPL", "SELL", "LIMIT", new BigDecimal("80"), new BigDecimal("151.00"), "TRADER004");
        createSampleOrder("AAPL", "SELL", "LIMIT", new BigDecimal("60"), new BigDecimal("151.50"), "TRADER005");
        createSampleOrder("AAPL", "SELL", "LIMIT", new BigDecimal("40"), new BigDecimal("152.00"), "TRADER006");
        
        // Create orders for another symbol
        createSampleOrder("GOOGL", "BUY", "LIMIT", new BigDecimal("25"), new BigDecimal("2800.00"), "TRADER007");
        createSampleOrder("GOOGL", "SELL", "LIMIT", new BigDecimal("30"), new BigDecimal("2810.00"), "TRADER008");
        
        log.info("Sample data initialization completed!");
    }
    
    private void createSampleOrder(String symbol, String side, String type, BigDecimal quantity, BigDecimal price, String traderId) {
        try {
            OrderRequest request = new OrderRequest();
            request.setSymbol(symbol);
            request.setSide(com.trading.engine.model.OrderSide.valueOf(side));
            request.setType(com.trading.engine.model.OrderType.valueOf(type));
            request.setQuantity(quantity);
            request.setPrice(price);
            request.setTraderId(traderId);
            
            orderService.placeOrder(request);
            log.info("Created sample order: {} {} {} {} @ {}", side, quantity, symbol, type, price);
        } catch (Exception e) {
            log.error("Error creating sample order: {}", e.getMessage());
        }
    }
} 