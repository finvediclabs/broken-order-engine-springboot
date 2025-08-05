package com.trading.engine.controller;

import com.trading.engine.model.OrderBook;
import com.trading.engine.service.MatchingEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarketDataController {
    
    private final MatchingEngineService matchingEngineService;
    
    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<Map<String, Object>> getOrderBook(@PathVariable String symbol) {
        OrderBook orderBook = matchingEngineService.getOrderBook(symbol);
        
        if (orderBook == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol);
        response.put("bestBid", orderBook.getBestBid());
        response.put("bestAsk", orderBook.getBestAsk());
        response.put("buyOrders", orderBook.getBuyOrders());
        response.put("sellOrders", orderBook.getSellOrders());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/price/{symbol}")
    public ResponseEntity<Map<String, Object>> getPriceData(@PathVariable String symbol) {
        OrderBook orderBook = matchingEngineService.getOrderBook(symbol);
        
        if (orderBook == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol);
        response.put("bestBid", orderBook.getBestBid());
        response.put("bestAsk", orderBook.getBestAsk());
        
        BigDecimal bestBid = orderBook.getBestBid();
        BigDecimal bestAsk = orderBook.getBestAsk();
        
        if (bestBid != null && bestAsk != null) {
            BigDecimal spread = bestAsk.subtract(bestBid);
            BigDecimal midPrice = bestBid.add(bestAsk).divide(BigDecimal.valueOf(2), 4, BigDecimal.ROUND_HALF_UP);
            response.put("spread", spread);
            response.put("midPrice", midPrice);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getAllSymbols() {
        Map<String, OrderBook> orderBooks = matchingEngineService.getAllOrderBooks();
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbols", orderBooks.keySet());
        response.put("count", orderBooks.size());
        
        return ResponseEntity.ok(response);
    }
} 