package app.tradeflows.api.market_data_service;

import app.tradeflows.api.market_data_service.clients.ExchangeServerClient;
import app.tradeflows.api.market_data_service.config.JsonBuilder;
import app.tradeflows.api.market_data_service.dto.exchange.ProductDTO;
import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.exceptions.NotFoundException;
import app.tradeflows.api.market_data_service.repositories.ProductRepository;
import app.tradeflows.api.market_data_service.services.ProductService;
import app.tradeflows.api.market_data_service.services.ProductHistoryService;
import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import app.tradeflows.api.market_data_service.services.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ProductServiceTests {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ExchangeServerClient exchangeServerClient;

    @Mock
    private ProductHistoryService productHistoryService;

    @Mock
    private RedisService<Object> redisService;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProduct() {
        Product product = new Product();
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        List<Product> result = productService.getAllProduct();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductByTicker() throws NotFoundException {
        String ticker = "AAPL";
        Product product = new Product();
        when(productRepository.findByTickerIgnoreCase(ticker)).thenReturn(Optional.of(product));

        Product result = productService.getProductByTicker(ticker);

        assertNotNull(result);
        verify(productRepository, times(1)).findByTickerIgnoreCase(ticker);
    }

    @Test
    void testGetProductByTickerNotFound() {
        String ticker = "AAPL";
        when(productRepository.findByTickerIgnoreCase(ticker)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductByTicker(ticker));
        verify(productRepository, times(1)).findByTickerIgnoreCase(ticker);
    }

    @Test
    void testGetProductById() throws NotFoundException {
        String id = "123";
        Product product = new Product();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(id);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void testGetProductByIdNotFound() {
        String id = "123";
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(id));
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void testGetProductFromExchangeAndStore() {
        ProductDTO dto1 = new ProductDTO(150, "AAPL", 145, 155, 200, 160, 165);
        ProductDTO dto2 = new ProductDTO(155, "AAPL", 150, 160, 210, 165, 170);
        List<ProductDTO> list1 = Collections.singletonList(dto1);
        List<ProductDTO> list2 = Collections.singletonList(dto2);

        when(exchangeServerClient.getProducts()).thenReturn(list1).thenReturn(list2);
        when(productRepository.findByTickerIgnoreCase(dto1.ticker())).thenReturn(Optional.empty());
        when(productRepository.findByTickerIgnoreCase(dto2.ticker())).thenReturn(Optional.empty());

        productService.getProductFromExchangeAndStore();

        verify(exchangeServerClient, times(2)).getProducts();
        verify(productRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetProductByTickerFromExchangeAndUpdate() {
        String ticker = "AAPL";
        ProductDTO dto1 = new ProductDTO(150, ticker, 145, 155, 200, 160, 165);
        ProductDTO dto2 = new ProductDTO(155, ticker, 150, 160, 210, 165, 170);

        when(exchangeServerClient.getProductTicker(ticker)).thenReturn(dto1).thenReturn(dto2);
        when(productRepository.findByTickerIgnoreCase(ticker)).thenReturn(Optional.empty());

        productService.getProductByTickerFromExchangeAndUpdate(ticker);

        verify(exchangeServerClient, times(2)).getProductTicker(ticker);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(redisService, times(4)).addItem(anyString(), any());
    }

    @Test
    void testGetProductWithHighestPrice() {
        ProductDTO dto1 = new ProductDTO( 150,"AAPL", 145, 155, 200, 160, 165);
        ProductDTO dto2 = new ProductDTO(155,"AAPL",  150, 160, 210, 165, 170);
        List<ProductDTO> list1 = Collections.singletonList(dto1);
        List<ProductDTO> list2 = Collections.singletonList(dto2);

        List<ProductDTO> result = productService.getProductWithHighestPrice(list1, list2);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dto2, result.get(0));
    }
}
