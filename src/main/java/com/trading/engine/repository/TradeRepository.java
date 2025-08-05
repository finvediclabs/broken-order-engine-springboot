package com.trading.engine.repository;

import com.trading.engine.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    Optional<Trade> findByTradeId(String tradeId);
    
    List<Trade> findBySymbol(String symbol);
    
    List<Trade> findBySymbolOrderByTimestampDesc(String symbol);
    
    List<Trade> findByBuyTraderIdOrSellTraderId(String buyTraderId, String sellTraderId);
} 