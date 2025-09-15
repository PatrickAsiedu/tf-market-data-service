package app.tradeflows.api.market_data_service.dto.exchange;

import app.tradeflows.api.market_data_service.enums.OrderSide;
import app.tradeflows.api.market_data_service.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OrderBookDTO(
        @JsonProperty(value = "product")
        String ticker,
        int quantity,
        double price,
        OrderSide side,
        List<ExecutionsDTO> executions,
        @JsonProperty(value = "orderID")
        String orderId,
        OrderType orderType,
        @JsonProperty(value = "cumulatitiveQuantity")
        int cumulativeQuantity,
        @JsonProperty(value = "cumulatitivePrice")
        double cumulativePrice
) {
}