package app.tradeflows.api.market_data_service.jobs;

import app.tradeflows.api.market_data_service.clients.ExchangeServerClient;
import app.tradeflows.api.market_data_service.config.JsonBuilder;
import app.tradeflows.api.market_data_service.dto.exchange.OrderBookDTO;
import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import app.tradeflows.api.market_data_service.enums.OrderBookFilter;
import app.tradeflows.api.market_data_service.services.ProductService;
import app.tradeflows.api.market_data_service.services.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderBookJob {

    private static final Logger logger = LoggerFactory.getLogger(OrderBookJob.class);
    private final ExchangeServerClient exchangeServerClient;
    private final ProductService productService;
    private final RedisService<List<OrderBookDTO>> redisService;

    public OrderBookJob(ExchangeServerClient exchangeServerClient, ProductService productService, RedisService<List<OrderBookDTO>> redisService) {
        this.exchangeServerClient = exchangeServerClient;
        this.productService = productService;
        this.redisService = redisService;
    }

    @Scheduled(cron = "*/20 * * * * ?")
    public void run(){
        logger.info("Running order book job to get the latest open orders and cache");
        this.cacheOrderBookInfo();
    }

    private void cacheOrderBookInfo(){
        productService.getAllProduct().forEach(product -> {
            try {
                getOrderBook(product.getTicker());
            }catch(Exception ex){
                logger.error(ex.toString(), ex);
            }
        });
        logger.info("Order book job completed");
    }

    private void getOrderBook(String ticker){
        exchangeServerClient.setServer(ExchangeServer.MAL1);
        List<OrderBookDTO> orderBookDTOS = exchangeServerClient.getOrderBooksByProduct(ticker, OrderBookFilter.OPEN);
        redisService.addItem("ORDER_BOOK_"+ticker+"_"+ExchangeServer.MAL1, orderBookDTOS);
        exchangeServerClient.setServer(ExchangeServer.MAL2);
        List<OrderBookDTO> orderBookDTOS2 = exchangeServerClient.getOrderBooksByProduct(ticker, OrderBookFilter.OPEN);
        redisService.addItem("ORDER_BOOK_"+ticker+"_"+ExchangeServer.MAL2, orderBookDTOS2);
    }
}
