package app.tradeflows.api.market_data_service;
import app.tradeflows.api.market_data_service.clients.ExchangeServerClient;
import app.tradeflows.api.market_data_service.config.KafkaProperties;
import app.tradeflows.api.market_data_service.dto.WebhookDTO;
import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import app.tradeflows.api.market_data_service.services.ProductService;
import app.tradeflows.api.market_data_service.services.RedisService;
import app.tradeflows.api.market_data_service.services.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SubscriptionServiceTests {

    @Mock
    private ProductService productService;

    @Mock
    private RedisService redisService;

    @Mock
    private ExchangeServerClient exchangeServerClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private List<WebSocketSession> webSocketSessions;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleRequest() {
        // Arrange
        WebhookDTO webhookDTO = new WebhookDTO();
        webhookDTO.setProduct("productTicker");

        Product product = new Product();
        product.setTicker("productTicker");

        when(productService.getProductByTickerFromExchangeAndUpdate(eq("productTicker"))).thenReturn(product);

        // Act
        subscriptionService.handleRequest(webhookDTO);

        // Assert
        verify(productService).getProductByTickerFromExchangeAndUpdate(eq("productTicker"));
        verify(redisService).addItem(eq("productTicker"), eq(product));
    }
}