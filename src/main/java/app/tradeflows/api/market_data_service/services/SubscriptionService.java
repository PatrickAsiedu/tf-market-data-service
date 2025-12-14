package app.tradeflows.api.market_data_service.services;

import app.tradeflows.api.market_data_service.clients.ExchangeServerClient;
import app.tradeflows.api.market_data_service.config.JsonBuilder;
import app.tradeflows.api.market_data_service.config.KafkaProperties;
import app.tradeflows.api.market_data_service.dto.WebhookDTO;
import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    private final ProductService productService;
    private final RedisService<Product> redisService;
    private final ExchangeServerClient exchangeServerClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final List<WebSocketSession> webSocketSessions;
    private final String webhookUrl;


    public SubscriptionService(
            ProductService productService,
            RedisService<Product> redisService,
            ExchangeServerClient exchangeServerClient,
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaProperties kafkaProperties,
            List<WebSocketSession> webSocketSessions,

            @Value("${exchange.webhook.url}") String webhookUrl) {
        this.productService = productService;
        this.redisService = redisService;
        this.exchangeServerClient = exchangeServerClient;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
        this.webSocketSessions = webSocketSessions;
        this.webhookUrl = webhookUrl;
    }

    @Async
    public void handleRequest(WebhookDTO webhookDTO){
        logger.info(webhookDTO.toString());
        var product = productService.getProductByTickerFromExchangeAndUpdate(webhookDTO.getProduct());
        // send to kafka
        String payload = new JsonBuilder().gson().toJson(product);
        kafkaTemplate.send(kafkaProperties.getMarketDataUpdateTopic(), payload);

        publishEnvelope("product_update", product, List.of("lastTradedPrice"));
    }

    /**
     * Publish product update notifications (redis, kafka, websocket) for the provided product.
     * This can be called from other components (e.g., scheduled jobs) to mimic webhook behavior.
     */
    public void publishMarketUpdate(Product product){
        try {
            publishEnvelope("product_update", product, null);
        } catch (Exception e){
            logger.error("Failed to publish product update: {}", e.toString());
        }
    }

    private void publishEnvelope(String type, Product product, List<String> fieldsChanged){
        try {

            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", type);
            envelope.put("timestamp", LocalDateTime.now().toString());
            envelope.put("changeType", "UPDATE");
            if(fieldsChanged != null) envelope.put("fieldsChanged", fieldsChanged);
            envelope.put("product", product);

            String payload = new JsonBuilder().gson().toJson(envelope);


            redisService.addItem(product.getTicker(), product);


            // send to websocket sessions
            webSocketSessions.forEach(session -> {
                if(session.isOpen()){
                    try {
                        logger.info("Sending market update to {}", session.getId());
                        session.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        logger.error(e.toString(), e);
                    }
                }
            });
        } catch (Exception e){
            logger.error("Failed to publish envelope: {}", e.toString());
        }
    }

    public void initializeWebhook(){
            try {
                exchangeServerClient.setServer(ExchangeServer.MAL1);
                logger.info("Checking for webhook subscription on {}", ExchangeServer.MAL1);
                List<String> webhookList = exchangeServerClient.checkWebhookSubscription();
                boolean isNotExisting = webhookList.stream().noneMatch(hook -> hook.equals(webhookUrl));
                if (isNotExisting) {
                    logger.info("Subscribing webhook since it does not exist {}", webhookUrl);
                    exchangeServerClient.subscribeWebhook(webhookUrl);
                }
            }catch(Exception exception){
                logger.error(exception.toString(), exception);
            }
            try {
                exchangeServerClient.setServer(ExchangeServer.MAL2);
                logger.info("Checking for webhook subscription on {}", ExchangeServer.MAL2);
                List<String> webhookList2 = exchangeServerClient.checkWebhookSubscription();
                boolean isNotExisting2 = webhookList2.stream().noneMatch(hook -> hook.equals(webhookUrl));
                if (isNotExisting2) {
                    logger.info("Subscribing webhook since it does not exist {}", webhookUrl);
                    exchangeServerClient.subscribeWebhook(webhookUrl);
                }
            }catch(Exception exception){
                logger.error(exception.toString(), exception);
            }
    }

}
