# Broken Order Matching Engine - Spring Boot Application

A high-performance simulated trading engine built with Spring Boot that implements a broken order matching system for financial markets.

## Features

- **Order Matching Engine**: Implements price-time priority matching algorithm
- **Multiple Order Types**: Support for Market, Limit, Stop, Stop-Limit, and Iceberg orders
- **Real-time Processing**: WebSocket support for real-time market data
- **RESTful API**: Complete REST API for order management and market data
- **In-Memory Database**: H2 database for fast data access
- **Order Book Management**: Efficient order book implementation with TreeMap
- **Trade Execution**: Automatic trade execution and settlement
- **Market Data**: Real-time price feeds and order book data

## Technology Stack

- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring WebSocket**
- **H2 Database**
- **Lombok**
- **Java 17**

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd broken-order-engine-springboot
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Application: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - API Documentation: Available at `/api/*` endpoints

## API Endpoints

### Order Management

#### Place Order
```http
POST /api/orders
Content-Type: application/json

{
  "symbol": "AAPL",
  "side": "BUY",
  "type": "LIMIT",
  "quantity": 100.0,
  "price": 150.50,
  "traderId": "TRADER001"
}
```

#### Get Order
```http
GET /api/orders/{orderId}
```

#### Get Orders by Symbol
```http
GET /api/orders/symbol/{symbol}
```

#### Get Orders by Trader
```http
GET /api/orders/trader/{traderId}
```

#### Cancel Order
```http
DELETE /api/orders/{orderId}
```

### Trade Management

#### Get Trade
```http
GET /api/trades/{tradeId}
```

#### Get Trades by Symbol
```http
GET /api/trades/symbol/{symbol}
```

#### Get Trades by Trader
```http
GET /api/trades/trader/{traderId}
```

### Market Data

#### Get Order Book
```http
GET /api/market/orderbook/{symbol}
```

#### Get Price Data
```http
GET /api/market/price/{symbol}
```

#### Get All Symbols
```http
GET /api/market/symbols
```

## Order Types

- **MARKET**: Executes immediately at the best available price
- **LIMIT**: Executes only at the specified price or better
- **STOP**: Becomes a market order when the stop price is reached
- **STOP_LIMIT**: Becomes a limit order when the stop price is reached
- **ICEBERG**: Large order split into smaller visible orders

## Order Sides

- **BUY**: Buy orders (bids)
- **SELL**: Sell orders (asks)

## Order Status

- **PENDING**: Order is waiting to be matched
- **PARTIALLY_FILLED**: Order has been partially executed
- **FILLED**: Order has been completely executed
- **CANCELLED**: Order has been cancelled
- **REJECTED**: Order was rejected
- **EXPIRED**: Order has expired

## Architecture

### Core Components

1. **MatchingEngineService**: Core matching logic
2. **OrderService**: Order management operations
3. **OrderBook**: In-memory order book implementation
4. **Controllers**: REST API endpoints
5. **Repositories**: Data access layer

### Data Flow

1. Client submits order via REST API
2. OrderService validates and creates order
3. MatchingEngineService processes order through matching algorithm
4. Orders are matched based on price-time priority
5. Trades are executed and saved
6. Order book is updated
7. Real-time updates sent via WebSocket

## Database Schema

### Orders Table
- `id`: Primary key
- `order_id`: Unique order identifier
- `symbol`: Trading symbol
- `side`: BUY or SELL
- `type`: Order type
- `quantity`: Order quantity
- `price`: Order price
- `status`: Order status
- `trader_id`: Trader identifier
- `timestamp`: Order timestamp
- `filled_quantity`: Quantity filled
- `average_price`: Average fill price

### Trades Table
- `id`: Primary key
- `trade_id`: Unique trade identifier
- `symbol`: Trading symbol
- `quantity`: Trade quantity
- `price`: Trade price
- `buy_order_id`: Buy order reference
- `sell_order_id`: Sell order reference
- `buy_trader_id`: Buy trader identifier
- `sell_trader_id`: Sell trader identifier
- `timestamp`: Trade timestamp
- `total_value`: Total trade value

## WebSocket Support

The application supports WebSocket connections for real-time updates:

- **Endpoint**: `/ws`
- **Topics**: `/topic/orders`, `/topic/trades`, `/topic/market-data`

## Monitoring

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

## Testing

### Sample API Calls

#### Place a Buy Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "side": "BUY",
    "type": "LIMIT",
    "quantity": 100.0,
    "price": 150.50,
    "traderId": "TRADER001"
  }'
```

#### Place a Sell Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "side": "SELL",
    "type": "LIMIT",
    "quantity": 50.0,
    "price": 150.00,
    "traderId": "TRADER002"
  }'
```

#### Get Order Book
```bash
curl http://localhost:8080/api/market/orderbook/AAPL
```

## Performance Considerations

- In-memory order books for fast matching
- ConcurrentHashMap for thread-safe operations
- Optimized matching algorithm
- Database indexing on frequently queried fields

## Security

- Input validation on all endpoints
- SQL injection protection via JPA
- CORS configuration for web clients

## Future Enhancements

- Support for more order types
- Advanced risk management
- Position tracking
- Margin requirements
- Regulatory compliance features
- Performance monitoring and alerts
- Multi-currency support
- Advanced analytics and reporting

## Known Bugs and Solutions

This application contains 4 intentional bugs for educational purposes. Here are the bugs and their solutions:

### Bug #1: NullPointerException in Average Price Calculation
**Location**: `MatchingEngineService.updateAveragePrice()`
**Issue**: When a new order is created, `averagePrice` is null, causing a NullPointerException when calculating the weighted average price.
**Solution**: Initialize `averagePrice` to `BigDecimal.ZERO` in the `Order` constructor or add null check:
```java
private void updateAveragePrice(Order order, BigDecimal tradeQuantity, BigDecimal tradePrice) {
    BigDecimal currentAveragePrice = order.getAveragePrice() != null ? 
        order.getAveragePrice() : BigDecimal.ZERO;
    BigDecimal totalValue = currentAveragePrice.multiply(order.getFilledQuantity().subtract(tradeQuantity))
            .add(tradeQuantity.multiply(tradePrice));
    BigDecimal newFilledQuantity = order.getFilledQuantity();
    
    if (newFilledQuantity.compareTo(BigDecimal.ZERO) > 0) {
        order.setAveragePrice(totalValue.divide(newFilledQuantity, 4, BigDecimal.ROUND_HALF_UP));
    }
}
```

### Bug #2: Incorrect Price Comparison for Market Orders
**Location**: `MatchingEngineService.matchBuyOrder()` and `matchSellOrder()`
**Issue**: The price comparison logic doesn't handle MARKET orders correctly. Market orders should match at any price, but the current logic only matches when buy price >= sell price.
**Solution**: Add special handling for MARKET orders:
```java
// For MARKET orders, always allow matching
if (buyOrder.getType() != OrderType.MARKET && 
    buyOrder.getPrice().compareTo(bestSellOrder.getPrice()) < 0) {
    break; // Buy price is lower than sell price
}
```

### Bug #3: Order Not Removed from In-Memory Order Book on Cancellation
**Location**: `OrderService.cancelOrder()`
**Issue**: When an order is cancelled, it's only updated in the database but not removed from the in-memory order book, causing inconsistencies.
**Solution**: Remove the order from the order book when cancelling:
```java
public OrderResponse cancelOrder(String orderId) {
    Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
    
    if (orderOpt.isPresent()) {
        Order order = orderOpt.get();
        
        if (order.getStatus() == OrderStatus.FILLED) {
            OrderResponse response = new OrderResponse();
            response.setSuccess(false);
            response.setMessage("Cannot cancel filled order");
            return response;
        }
        
        // Remove from in-memory order book
        OrderBook orderBook = matchingEngineService.getOrderBook(order.getSymbol());
        if (orderBook != null) {
            orderBook.removeOrder(order);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        // ... rest of the method
    }
}
```

### Bug #4: Missing Input Validation for Negative Values
**Location**: `OrderController.placeOrder()`
**Issue**: The controller doesn't validate that quantities and prices are positive, allowing negative values to be processed.
**Solution**: Add validation in the controller or DTO:
```java
@PostMapping
public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
    // Add validation
    if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
        OrderResponse response = new OrderResponse();
        response.setSuccess(false);
        response.setMessage("Quantity must be positive");
        return ResponseEntity.badRequest().body(response);
    }
    
    if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
        OrderResponse response = new OrderResponse();
        response.setSuccess(false);
        response.setMessage("Price must be positive");
        return ResponseEntity.badRequest().body(response);
    }
    
    OrderResponse response = orderService.placeOrder(request);
    
    if (response.isSuccess()) {
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.badRequest().body(response);
    }
}
```

## License

This project is licensed under the MIT License.