package app.tradeflows.api.market_data_service.clients;

import app.tradeflows.api.market_data_service.config.ExchangeServerProperties;
import app.tradeflows.api.market_data_service.dto.exchange.OrderBookDTO;
import app.tradeflows.api.market_data_service.dto.exchange.ProductDTO;
import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import app.tradeflows.api.market_data_service.enums.OrderBookFilter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
public class ExchangeServerClient {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeServerClient.class);
    private final RestTemplate restTemplate;
    @Setter
    private ExchangeServer server;
    private final ExchangeServerProperties serverProperties;

    public ExchangeServerClient(ExchangeServerProperties serverProperties){
        this.restTemplate = new RestTemplate();
        this.serverProperties = serverProperties;
    }

    private String getExchangeServerBaseUrl(){
        if(Objects.equals(server.getType(), "MAL1")){
            return serverProperties.getBaseUrl1();
        }

        return serverProperties.getBaseUrl2();
    }

    private String getExchangeServerKey(){
        if(Objects.equals(server.getType(), "MAL1")){
            return serverProperties.getApiKey1();
        }

        return  serverProperties.getApiKey2();
    }

    // Get Products
    public List<ProductDTO> getProducts(){
        var url = getExchangeServerBaseUrl()+"/pd";
        logger.info("Getting all products from {}", url);
        var response = this.restTemplate.exchange(url,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ProductDTO>>() {});
        return response.getBody();
    }

    // Get Product by Ticker
    public ProductDTO getProductTicker(String ticker){
        var url = getExchangeServerBaseUrl()+"/pd/"+ticker;
        logger.info("Getting product by ticker from {}", url);
        var response = this.restTemplate.exchange(getExchangeServerBaseUrl()+"/pd/"+ticker,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<ProductDTO>() {});
        return response.getBody();
    }

    // Check subscription
    public List<String> checkWebhookSubscription(){
        var url = getExchangeServerBaseUrl()+"/pd/subscription";
        logger.info("Getting list of subscription {}", url);
        var response = this.restTemplate.exchange(url,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<String>>() {});
        return response.getBody();
    }

    // Subscribe to product notification
    public Boolean subscribeWebhook(String webhookUrl){
        var url = getExchangeServerBaseUrl()+"/pd/subscription";
        logger.info("Subscribing to webhook {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(webhookUrl, headers);
        var response = this.restTemplate.exchange(url,
                HttpMethod.POST, request,
                new ParameterizedTypeReference<Boolean>() {});
        return response.getBody();
    }

    // Get orderBooks
    public List<OrderBookDTO> getAllOrderBooks(){
        var url = getExchangeServerBaseUrl()+"/orderbook";
        var response = this.restTemplate.exchange(url,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<OrderBookDTO>>() {});
        return response.getBody();
    }

    // Get orderBook by product and filter (buy/sell/open/closed/cancelled)
    public List<OrderBookDTO> getOrderBooksByProduct(String ticker, OrderBookFilter filter){
        var url = getExchangeServerBaseUrl()+"/orderbook/"+ticker+"/"+filter.getFilter();
        var response = this.restTemplate.exchange(url,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<OrderBookDTO>>() {});
        return response.getBody();
    }

    // Create Order

    //Check Order Status

    //Cancel Order

    //Update order price and quantity
}
