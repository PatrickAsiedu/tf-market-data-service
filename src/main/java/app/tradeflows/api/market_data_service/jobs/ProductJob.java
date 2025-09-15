package app.tradeflows.api.market_data_service.jobs;

import app.tradeflows.api.market_data_service.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProductJob {

    private static final Logger logger = LoggerFactory.getLogger(ProductJob.class);
    private final ProductService productService;

    public ProductJob(ProductService productService) {
        this.productService = productService;
    }

    @Scheduled(cron = "* */10 * * * ?")
    public void run(){
        logger.info("Running product job to get the latest product info");
        this.getLatestProductInfo();
    }

    private void getLatestProductInfo(){
        productService.getAllProduct().forEach( product -> {
            try {
                productService.getProductByTickerFromExchangeAndUpdate(product.getTicker());
            }catch (Exception exception){
                logger.error(exception.toString(), exception);
            }
        });
        logger.info("Product job completed");
    }
}
