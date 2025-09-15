package app.tradeflows.api.market_data_service.controllers;

import app.tradeflows.api.market_data_service.dto.ChartDataDTO;
import app.tradeflows.api.market_data_service.entities.ProductHistory;
import app.tradeflows.api.market_data_service.services.ProductHistoryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/product-history")
public class ProductHistoryController {

    private final ProductHistoryService productHistoryService;

    public ProductHistoryController(ProductHistoryService productHistoryService) {
        this.productHistoryService = productHistoryService;
    }

    @GetMapping(value="/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductHistory>> getProductHistories(){
        return ResponseEntity.ok(productHistoryService.getProductHistory());
    }

    @GetMapping(value="/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductHistory>> getProductHistoriesByProduct(@PathVariable String productId){
        return ResponseEntity.ok(productHistoryService.getProductHistoryById(productId));
    }

    @GetMapping(value="/chart-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChartDataDTO>> getProductHistoryChartData(){
        return ResponseEntity.ok(productHistoryService.getChartData());
    }

}
