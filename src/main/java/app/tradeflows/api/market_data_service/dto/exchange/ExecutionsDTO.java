package app.tradeflows.api.market_data_service.dto.exchange;

import java.time.LocalDateTime;

public record ExecutionsDTO (
        LocalDateTime timestamp,
        double price,
        int quantity
) {
}


