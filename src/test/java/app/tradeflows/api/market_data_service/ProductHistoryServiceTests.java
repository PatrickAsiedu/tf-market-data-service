package app.tradeflows.api.market_data_service;

import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.entities.ProductHistory;
import app.tradeflows.api.market_data_service.repositories.ProductHistoryRepository;
import app.tradeflows.api.market_data_service.services.ProductHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ProductHistoryServiceTests {
    @Mock
    private ProductHistoryRepository productHistoryRepository;

    @InjectMocks
    private ProductHistoryService productHistoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveProductHistory() {
        // Arrange
        Product product = new Product();
        product.setBidPrice(100.0);
        product.setAskPrice(105.0);
        product.setBuyLimit(110);
        product.setLastTradedPrice(102.0);
        product.setSellLimit(108);
        product.setMaxShiftPrice(5.0);

        // Act
        productHistoryService.saveProductHistory(product);

        // Assert
        verify(productHistoryRepository).save(any(ProductHistory.class));

        // Optionally, verify the properties of the saved ProductHistory object
        ArgumentCaptor<ProductHistory> captor = ArgumentCaptor.forClass(ProductHistory.class);
        verify(productHistoryRepository).save(captor.capture());

        ProductHistory savedHistory = captor.getValue();
        assertEquals(product, savedHistory.getProduct());
        assertEquals(product.getBidPrice(), savedHistory.getBidPrice());
        assertEquals(product.getAskPrice(), savedHistory.getAskPrice());
        assertEquals(product.getBuyLimit(), savedHistory.getBuyLimit());
        assertEquals(product.getLastTradedPrice(), savedHistory.getLastTradedPrice());
        assertEquals(product.getSellLimit(), savedHistory.getSellLimit());
        assertEquals(product.getMaxShiftPrice(), savedHistory.getMaxShiftPrice());
        assertNotNull(savedHistory.getCreatedAt());
    }

    @Test
    void testGetProductHistoryById() {
        // Arrange
        String productId = "product123";
        ProductHistory history1 = new ProductHistory();
        ProductHistory history2 = new ProductHistory();
        List<ProductHistory> historyList = List.of(history1, history2);
        when(productHistoryRepository.findByProduct_IdOrderByCreatedAtAsc(eq(productId))).thenReturn(historyList);

        // Act
        List<ProductHistory> result = productHistoryService.getProductHistoryById(productId);

        // Assert
        verify(productHistoryRepository).findByProduct_IdOrderByCreatedAtAsc(eq(productId));
        assertEquals(2, result.size());
        assertTrue(result.contains(history1));
        assertTrue(result.contains(history2));
    }

    @Test
    void testGetProductHistory() {
        // Arrange
        ProductHistory history1 = new ProductHistory();
        ProductHistory history2 = new ProductHistory();
        List<ProductHistory> historyList = List.of(history1, history2);
        when(productHistoryRepository.findByProduct_TickerNotNullOrderByCreatedAtAsc()).thenReturn(historyList);

        // Act
        List<ProductHistory> result = productHistoryService.getProductHistory();

        // Assert
        verify(productHistoryRepository).findByProduct_TickerNotNullOrderByCreatedAtAsc();
        assertEquals(2, result.size());
        assertTrue(result.contains(history1));
        assertTrue(result.contains(history2));
    }
}
