package com.trading.engine.service;

import com.trading.engine.dto.OrderResponse;
import com.trading.engine.model.*;
import com.trading.engine.repository.OrderRepository;
import com.trading.engine.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingEngineService {
    
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    
    @Transactional
    public OrderResponse processOrder(Order order) {
        OrderResponse response = new OrderResponse();
        
        try {
            // Validate order
            if (!validateOrder(order)) {
                response.setSuccess(false);
                response.setMessage("Invalid order parameters");
                return response;
            }
            
            // Save order to database
            order = orderRepository.save(order);
            
            // Get or create order book for symbol
            OrderBook orderBook = orderBooks.computeIfAbsent(order.getSymbol(), OrderBook::new);
            
            // Add order to order book
            orderBook.addOrder(order);
            
            // Attempt to match orders
            List<Trade> trades = matchOrders(orderBook, order);
            
            // Save trades
            if (!trades.isEmpty()) {
                tradeRepository.saveAll(trades);
                log.info("Executed {} trades for order {}", trades.size(), order.getOrderId());
            }
            
            // Update order status
            updateOrderStatus(order);
            orderRepository.save(order);
            
            // Build response
            buildOrderResponse(order, response, true, "Order processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Error processing order: " + e.getMessage());
        }
        
        return response;
    }
    
    private boolean validateOrder(Order order) {
        return order.getSymbol() != null && !order.getSymbol().trim().isEmpty() &&
               order.getQuantity() != null && order.getQuantity().compareTo(BigDecimal.ZERO) > 0 &&
               order.getPrice() != null && order.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
               order.getTraderId() != null && !order.getTraderId().trim().isEmpty();
    }
    
    private List<Trade> matchOrders(OrderBook orderBook, Order newOrder) {
        List<Trade> trades = new ArrayList<>();
        
        if (newOrder.getSide() == OrderSide.BUY) {
            trades.addAll(matchBuyOrder(orderBook, newOrder));
        } else {
            trades.addAll(matchSellOrder(orderBook, newOrder));
        }
        
        return trades;
    }
    
    private List<Trade> matchBuyOrder(OrderBook orderBook, Order buyOrder) {
        List<Trade> trades = new ArrayList<>();
        BigDecimal remainingQuantity = buyOrder.getQuantity().subtract(buyOrder.getFilledQuantity());
        
        while (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            List<Order> bestSellOrders = orderBook.getBestAskOrders();
            
            if (bestSellOrders.isEmpty()) {
                break; // No matching sell orders
            }
            
            Order bestSellOrder = bestSellOrders.get(0);
            
            // BUG #2: Incorrect price comparison - should be >= for market orders
            if (buyOrder.getPrice().compareTo(bestSellOrder.getPrice()) < 0) {
                break; // Buy price is lower than sell price
            }
            
            // Calculate trade quantity
            BigDecimal tradeQuantity = remainingQuantity.min(
                bestSellOrder.getQuantity().subtract(bestSellOrder.getFilledQuantity())
            );
            
            // Execute trade
            Trade trade = executeTrade(buyOrder, bestSellOrder, tradeQuantity, bestSellOrder.getPrice());
            trades.add(trade);
            
            // Update order quantities
            updateOrderQuantities(buyOrder, bestSellOrder, tradeQuantity, bestSellOrder.getPrice());
            
            remainingQuantity = remainingQuantity.subtract(tradeQuantity);
            
            // Remove filled sell order from order book
            if (bestSellOrder.getFilledQuantity().compareTo(bestSellOrder.getQuantity()) >= 0) {
                orderBook.removeOrder(bestSellOrder);
                bestSellOrder.setStatus(OrderStatus.FILLED);
                orderRepository.save(bestSellOrder);
            }
        }
        
        return trades;
    }
    
    private List<Trade> matchSellOrder(OrderBook orderBook, Order sellOrder) {
        List<Trade> trades = new ArrayList<>();
        BigDecimal remainingQuantity = sellOrder.getQuantity().subtract(sellOrder.getFilledQuantity());
        
        while (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            List<Order> bestBuyOrders = orderBook.getBestBidOrders();
            
            if (bestBuyOrders.isEmpty()) {
                break; // No matching buy orders
            }
            
            Order bestBuyOrder = bestBuyOrders.get(0);
            
            // Check if prices match
            if (sellOrder.getPrice().compareTo(bestBuyOrder.getPrice()) > 0) {
                break; // Sell price is higher than buy price
            }
            
            // Calculate trade quantity
            BigDecimal tradeQuantity = remainingQuantity.min(
                bestBuyOrder.getQuantity().subtract(bestBuyOrder.getFilledQuantity())
            );
            
            // Execute trade
            Trade trade = executeTrade(bestBuyOrder, sellOrder, tradeQuantity, bestBuyOrder.getPrice());
            trades.add(trade);
            
            // Update order quantities
            updateOrderQuantities(bestBuyOrder, sellOrder, tradeQuantity, bestBuyOrder.getPrice());
            
            remainingQuantity = remainingQuantity.subtract(tradeQuantity);
            
            // Remove filled buy order from order book
            if (bestBuyOrder.getFilledQuantity().compareTo(bestBuyOrder.getQuantity()) >= 0) {
                orderBook.removeOrder(bestBuyOrder);
                bestBuyOrder.setStatus(OrderStatus.FILLED);
                orderRepository.save(bestBuyOrder);
            }
        }
        
        return trades;
    }
    
    private Trade executeTrade(Order buyOrder, Order sellOrder, BigDecimal quantity, BigDecimal price) {
        Trade trade = new Trade();
        trade.setTradeId(generateTradeId());
        trade.setSymbol(buyOrder.getSymbol());
        trade.setQuantity(quantity);
        trade.setPrice(price);
        trade.setBuyOrderId(buyOrder.getOrderId());
        trade.setSellOrderId(sellOrder.getOrderId());
        trade.setBuyTraderId(buyOrder.getTraderId());
        trade.setSellTraderId(sellOrder.getTraderId());
        trade.setTimestamp(LocalDateTime.now());
        trade.setTotalValue(quantity.multiply(price));
        
        return trade;
    }
    
    private void updateOrderQuantities(Order buyOrder, Order sellOrder, BigDecimal tradeQuantity, BigDecimal price) {
        // Update buy order
        BigDecimal newFilledQuantity = buyOrder.getFilledQuantity().add(tradeQuantity);
        buyOrder.setFilledQuantity(newFilledQuantity);
        
        if (newFilledQuantity.compareTo(buyOrder.getQuantity()) >= 0) {
            buyOrder.setStatus(OrderStatus.FILLED);
        } else {
            buyOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
        
        // Update average price for buy order
        updateAveragePrice(buyOrder, tradeQuantity, price);
        
        // Update sell order
        BigDecimal sellFilledQuantity = sellOrder.getFilledQuantity().add(tradeQuantity);
        sellOrder.setFilledQuantity(sellFilledQuantity);
        
        if (sellFilledQuantity.compareTo(sellOrder.getQuantity()) >= 0) {
            sellOrder.setStatus(OrderStatus.FILLED);
        } else {
            sellOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
        
        // Update average price for sell order
        updateAveragePrice(sellOrder, tradeQuantity, price);
        
        // Save updated orders
        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);
    }
    
    private void updateAveragePrice(Order order, BigDecimal tradeQuantity, BigDecimal tradePrice) {
        // BUG #1: NullPointerException when averagePrice is null for new orders
        BigDecimal totalValue = order.getAveragePrice().multiply(order.getFilledQuantity().subtract(tradeQuantity))
                .add(tradeQuantity.multiply(tradePrice));
        BigDecimal newFilledQuantity = order.getFilledQuantity();
        
        if (newFilledQuantity.compareTo(BigDecimal.ZERO) > 0) {
            order.setAveragePrice(totalValue.divide(newFilledQuantity, 4, BigDecimal.ROUND_HALF_UP));
        }
    }
    
    private void updateOrderStatus(Order order) {
        if (order.getStatus() == null) {
            if (order.getFilledQuantity().compareTo(BigDecimal.ZERO) == 0) {
                order.setStatus(OrderStatus.PENDING);
            } else if (order.getFilledQuantity().compareTo(order.getQuantity()) >= 0) {
                order.setStatus(OrderStatus.FILLED);
            } else {
                order.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
        }
    }
    
    private void buildOrderResponse(Order order, OrderResponse response, boolean success, String message) {
        response.setOrderId(order.getOrderId());
        response.setSymbol(order.getSymbol());
        response.setSide(order.getSide().name());
        response.setType(order.getType().name());
        response.setQuantity(order.getQuantity());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setTraderId(order.getTraderId());
        response.setTimestamp(order.getTimestamp());
        response.setFilledQuantity(order.getFilledQuantity());
        response.setAveragePrice(order.getAveragePrice());
        response.setSuccess(success);
        response.setMessage(message);
    }
    
    private String generateTradeId() {
        return "TRADE_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public OrderBook getOrderBook(String symbol) {
        return orderBooks.get(symbol);
    }
    
    public Map<String, OrderBook> getAllOrderBooks() {
        return new HashMap<>(orderBooks);
    }
} 