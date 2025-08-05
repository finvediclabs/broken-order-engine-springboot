package com.trading.engine.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {
    
    private String symbol;
    private TreeMap<BigDecimal, List<Order>> buyOrders; // Price -> Orders (descending)
    private TreeMap<BigDecimal, List<Order>> sellOrders; // Price -> Orders (ascending)
    
    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyOrders = new TreeMap<>(Collections.reverseOrder()); // Highest price first
        this.sellOrders = new TreeMap<>(); // Lowest price first
    }
    
    public void addOrder(Order order) {
        TreeMap<BigDecimal, List<Order>> orders = 
            order.getSide() == OrderSide.BUY ? buyOrders : sellOrders;
        
        orders.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
    }
    
    public void removeOrder(Order order) {
        TreeMap<BigDecimal, List<Order>> orders = 
            order.getSide() == OrderSide.BUY ? buyOrders : sellOrders;
        
        List<Order> orderList = orders.get(order.getPrice());
        if (orderList != null) {
            orderList.removeIf(o -> o.getOrderId().equals(order.getOrderId()));
            if (orderList.isEmpty()) {
                orders.remove(order.getPrice());
            }
        }
    }
    
    public BigDecimal getBestBid() {
        return buyOrders.isEmpty() ? null : buyOrders.firstKey();
    }
    
    public BigDecimal getBestAsk() {
        return sellOrders.isEmpty() ? null : sellOrders.firstKey();
    }
    
    public List<Order> getBestBidOrders() {
        return buyOrders.isEmpty() ? new ArrayList<>() : buyOrders.firstEntry().getValue();
    }
    
    public List<Order> getBestAskOrders() {
        return sellOrders.isEmpty() ? new ArrayList<>() : sellOrders.firstEntry().getValue();
    }
    
    public boolean hasCrossedSpread() {
        BigDecimal bestBid = getBestBid();
        BigDecimal bestAsk = getBestAsk();
        return bestBid != null && bestAsk != null && bestBid.compareTo(bestAsk) >= 0;
    }
} 