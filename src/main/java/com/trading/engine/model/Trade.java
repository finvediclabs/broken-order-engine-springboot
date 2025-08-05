package com.trading.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String tradeId;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    
    @Column(nullable = false)
    private String buyOrderId;
    
    @Column(nullable = false)
    private String sellOrderId;
    
    @Column(nullable = false)
    private String buyTraderId;
    
    @Column(nullable = false)
    private String sellTraderId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal totalValue;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        if (totalValue == null) {
            totalValue = quantity.multiply(price);
        }
    }
} 