package com.trading.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private String traderId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal filledQuantity = BigDecimal.ZERO;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal averagePrice = BigDecimal.ZERO;
    
    @Column
    private LocalDateTime lastModified;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        lastModified = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
    }
} 