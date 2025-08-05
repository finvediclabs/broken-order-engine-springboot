package com.trading.engine.dto;

import com.trading.engine.model.OrderSide;
import com.trading.engine.model.OrderType;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class OrderRequest {
    
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotNull(message = "Order side is required")
    private OrderSide side;
    
    @NotNull(message = "Order type is required")
    private OrderType type;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0001", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotBlank(message = "Trader ID is required")
    private String traderId;
} 