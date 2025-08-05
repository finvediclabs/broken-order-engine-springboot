package com.trading.engine.controller;

import com.trading.engine.dto.TradeResponse;
import com.trading.engine.model.Trade;
import com.trading.engine.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TradeController {
    
    private final TradeRepository tradeRepository;
    
    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable String tradeId) {
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(tradeId);
        
        if (tradeOpt.isPresent()) {
            Trade trade = tradeOpt.get();
            TradeResponse response = new TradeResponse();
            response.setTradeId(trade.getTradeId());
            response.setSymbol(trade.getSymbol());
            response.setQuantity(trade.getQuantity());
            response.setPrice(trade.getPrice());
            response.setBuyOrderId(trade.getBuyOrderId());
            response.setSellOrderId(trade.getSellOrderId());
            response.setBuyTraderId(trade.getBuyTraderId());
            response.setSellTraderId(trade.getSellTraderId());
            response.setTimestamp(trade.getTimestamp());
            response.setTotalValue(trade.getTotalValue());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Trade>> getTradesBySymbol(@PathVariable String symbol) {
        List<Trade> trades = tradeRepository.findBySymbolOrderByTimestampDesc(symbol);
        return ResponseEntity.ok(trades);
    }
    
    @GetMapping("/trader/{traderId}")
    public ResponseEntity<List<Trade>> getTradesByTrader(@PathVariable String traderId) {
        List<Trade> trades = tradeRepository.findByBuyTraderIdOrSellTraderId(traderId, traderId);
        return ResponseEntity.ok(trades);
    }
} 