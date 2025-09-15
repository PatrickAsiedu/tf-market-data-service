package app.tradeflows.api.market_data_service.dto;

import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import app.tradeflows.api.market_data_service.enums.OrderSide;
import app.tradeflows.api.market_data_service.enums.OrderType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebhookDTO {
    private OrderType orderType;
    private String product;
    private OrderSide side;
    private String orderID;
    private double price;
    private int qty;
    private int cumQty;
    private double cumPrx;
    private ExchangeServer exchange;
    private LocalDateTime timestamp;
}
