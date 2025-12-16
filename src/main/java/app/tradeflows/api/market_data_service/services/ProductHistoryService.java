package app.tradeflows.api.market_data_service.services;

import app.tradeflows.api.market_data_service.dto.ChartDataDTO;
import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.entities.ProductHistory;
import app.tradeflows.api.market_data_service.repositories.ProductHistoryRepository;
import app.tradeflows.api.market_data_service.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductHistoryService {

    private final ProductHistoryRepository productHistoryRepository;
    private final ProductRepository productRepository;

    public ProductHistoryService(ProductHistoryRepository productHistoryRepository, ProductRepository productRepository) {
        this.productHistoryRepository = productHistoryRepository;
        this.productRepository = productRepository;
    }

    public void saveProductHistory(Product product){
        ProductHistory productHistory = new ProductHistory();
        productHistory.setProduct(product);
        productHistory.setBidPrice(product.getBidPrice());
        productHistory.setAskPrice(product.getAskPrice());
        productHistory.setBuyLimit(product.getBuyLimit());
        productHistory.setLastTradedPrice(product.getLastTradedPrice());
        productHistory.setSellLimit(product.getSellLimit());
        productHistory.setMaxShiftPrice(product.getMaxShiftPrice());
        productHistory.setCreatedAt(LocalDateTime.now());

        productHistoryRepository.save(productHistory);
    }

    public List<ProductHistory> getProductHistoryById(String productId){
        return productHistoryRepository.findByProduct_IdOrderByCreatedAtAsc(productId);
    }

    public List<ProductHistory> getProductHistory(){
        return productHistoryRepository.findByProduct_TickerNotNullOrderByCreatedAtAsc();
    }

    public List<ChartDataDTO> getChartData(){
        LocalDateTime hoursAgo = LocalDateTime.now().minusHours(6);
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> {
            ChartDataDTO chartDataDTO = new ChartDataDTO();
            chartDataDTO.setName(product.getTicker());
            chartDataDTO.setColor(this.getProductColor(product.getTicker()));
            chartDataDTO.setData(productHistoryRepository.
                    findByProduct_IdAndCreatedAtAfterOrderByCreatedAtAsc(product.getId(), hoursAgo).stream().map( productHistory -> {
                        List<Object> item = new ArrayList<>();
                        item.add(productHistory.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        DecimalFormat df = new DecimalFormat("#.##");
                        item.add(Double.valueOf(df.format(productHistory.getLastTradedPrice())));
                        return item;
                    }).toList());
            return chartDataDTO;
        }).toList();
    }

    private String getProductColor(String ticker){
        switch (ticker){
            case "IBM" -> {
                return "#054ADA";
            }
            case "NFLX" -> {
                return "#E50914";
            }
            case "AAPL" -> {
                return "#A2AAAD";
            }
            case "TSLA" -> {
                return "#CC0000";
            }
            case "MSFT" -> {
                return "#0078D4";
            }
            case "AMZN" -> {
                return "#FF9900";
            }
            case "GOOGL" -> {
                return "#4285F4";
            }
            case "ORCL" -> {
                return "#F80000";
            }
            default -> {
                return "";
            }
        }
    }
}
