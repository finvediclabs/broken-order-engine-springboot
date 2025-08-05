package com.trading.engine.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeResponse {
    private String tradeId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private String buyOrderId;
    private String sellOrderId;
    private String buyTraderId;
    private String sellTraderId;
    private LocalDateTime timestamp;
    private BigDecimal totalValue;
} 