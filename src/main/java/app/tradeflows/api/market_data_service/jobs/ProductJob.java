package app.tradeflows.api.market_data_service.jobs;

import app.tradeflows.api.market_data_service.services.ProductService;
import app.tradeflows.api.market_data_service.services.SubscriptionService;
import app.tradeflows.api.market_data_service.config.SocketConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProductJob {

    private static final Logger logger = LoggerFactory.getLogger(ProductJob.class);
    private final ProductService productService;
    private final SubscriptionService subscriptionService;

    public ProductJob(ProductService productService, SocketConnectionHandler socketConnectionHandler, SubscriptionService subscriptionService) {
        this.productService = productService;
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void run(){
        logger.info("Running product job to get the latest product info");
        this.getLatestProductInfo();
    }

    private void getLatestProductInfo(){
        productService.getAllProduct().forEach( product -> {
            try {
                var updated = productService.getProductByTickerFromExchangeAndUpdate(product.getTicker());
                // publish webhook-like update per product
                subscriptionService.publishMarketUpdate(updated);
            }catch (Exception exception){
                logger.error(exception.toString(), exception);
            }
        });
        logger.info("Product job completed");
        try {
            // Broadcast the latest product list to all websocket clients
            productService.broadcastProducts(productService.getAllProduct());
        } catch (Exception e) {
            logger.error("Failed to broadcast product list: {}", e.toString());
        }
    }
}
