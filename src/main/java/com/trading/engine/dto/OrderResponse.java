package com.trading.engine.dto;

import com.trading.engine.model.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private String orderId;
    private String symbol;
    private String side;
    private String type;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderStatus status;
    private String traderId;
    private LocalDateTime timestamp;
    private BigDecimal filledQuantity;
    private BigDecimal averagePrice;
    private String message;
    private boolean success;
} 