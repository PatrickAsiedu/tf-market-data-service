package app.tradeflows.api.market_data_service.event_listers;

import app.tradeflows.api.market_data_service.config.ExchangeServerProperties;
import app.tradeflows.api.market_data_service.services.ProductService;
import app.tradeflows.api.market_data_service.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StartupApplicationListener.class);
    private final ProductService productService;
    private final SubscriptionService subscriptionService;
    private final ExchangeServerProperties exchangeServerProperties;

    public StartupApplicationListener( ProductService productService, SubscriptionService subscriptionService, ExchangeServerProperties exchangeServerProperties) {
        this.subscriptionService = subscriptionService;
        this.productService = productService;
        this.exchangeServerProperties = exchangeServerProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Your startup logic here
        try {
            logger.info("Application is ready.");
            subscriptionService.initializeWebhook();
            if(exchangeServerProperties.isInitializeProducts()) {
                System.out.println("========================================");
                productService.getProductFromExchangeAndStore();
            }
        }catch (Exception ex){
            logger.error(ex.toString(), ex);
        }
    }
}