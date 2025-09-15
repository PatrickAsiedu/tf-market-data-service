package app.tradeflows.api.market_data_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartDataDTO {
    private String color;
    private String name;
    private List<List<Object>> data;
}
