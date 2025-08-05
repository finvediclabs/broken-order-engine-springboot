package com.trading.engine.repository;

import com.trading.engine.model.Order;
import com.trading.engine.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderId(String orderId);
    
    List<Order> findBySymbolAndStatus(String symbol, OrderStatus status);
    
    List<Order> findByTraderId(String traderId);
    
    List<Order> findBySymbol(String symbol);
    
    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.status IN ('PENDING', 'PARTIALLY_FILLED') ORDER BY o.timestamp ASC")
    List<Order> findActiveOrdersBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.side = 'BUY' AND o.status IN ('PENDING', 'PARTIALLY_FILLED') ORDER BY o.price DESC, o.timestamp ASC")
    List<Order> findActiveBuyOrdersBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.side = 'SELL' AND o.status IN ('PENDING', 'PARTIALLY_FILLED') ORDER BY o.price ASC, o.timestamp ASC")
    List<Order> findActiveSellOrdersBySymbol(@Param("symbol") String symbol);
} 