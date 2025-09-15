package app.tradeflows.api.market_data_service.services;

import app.tradeflows.api.market_data_service.clients.ExchangeServerClient;
import app.tradeflows.api.market_data_service.config.ExchangeServerProperties;
import app.tradeflows.api.market_data_service.config.JsonBuilder;
import app.tradeflows.api.market_data_service.dto.exchange.ProductDTO;
import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.enums.ExchangeServer;
import app.tradeflows.api.market_data_service.exceptions.NotFoundException;
import app.tradeflows.api.market_data_service.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ExchangeServerClient exchangeServerClient;
    private final ProductHistoryService productHistoryService;
    private final RedisService<Object> redisService;


    public ProductService(ProductRepository productRepository, ExchangeServerClient exchangeServerClient, ProductHistoryService productHistoryService,RedisService<Object> redisService){
        this.productRepository = productRepository;
        this.exchangeServerClient = exchangeServerClient;
        this.productHistoryService = productHistoryService;
        this.redisService = redisService;
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductByTicker(String ticker) throws NotFoundException {
        Optional<Product> product = productRepository.findByTickerIgnoreCase(ticker);

        return product.orElseThrow(() -> new NotFoundException("Product with specified ticker does not exist"));
    }

    public Product getProductById(String id) throws NotFoundException {
        Optional<Product> product = productRepository.findById(id);

        return product.orElseThrow(() -> new NotFoundException("Product with specified id does not exist"));
    }


    public void getProductFromExchangeAndStore(){
        exchangeServerClient.setServer(ExchangeServer.MAL1);
        List<ProductDTO> productDTOS = exchangeServerClient.getProducts();

        exchangeServerClient.setServer(ExchangeServer.MAL2);
        List<ProductDTO> productDTO2S = exchangeServerClient.getProducts();

        logger.info(productDTOS.toString());
        logger.info(productDTO2S.toString());
        var preferredList = getProductWithHighestPrice(productDTOS, productDTO2S);

        var mappedProduct = preferredList.stream().map(list -> {
            Optional<Product> existingProduct = productRepository.findByTickerIgnoreCase(list.ticker());
            if(existingProduct.isPresent()){
                Product product = existingProduct.get();
                product.setAskPrice(list.askPrice());
                product.setBuyLimit(list.buyLimit());
                product.setBidPrice(list.bidPrice());
                product.setTicker(list.ticker());
                product.setLastTradedPrice(list.lastTradedPrice());
                product.setMaxShiftPrice(list.maxPriceShift());
                product.setSellLimit(list.sellLimit());
                product.setTrading(true);
                redisService.addItem(list.ticker(), new JsonBuilder().gson().toJson(product));
                return product;
            }
            Product product = new Product();
            product.setAskPrice(list.askPrice());
            product.setBuyLimit(list.buyLimit());
            product.setBidPrice(list.bidPrice());
            product.setTicker(list.ticker());
            product.setLastTradedPrice(list.lastTradedPrice());
            product.setMaxShiftPrice(list.maxPriceShift());
            product.setSellLimit(list.sellLimit());
            product.setTrading(true);
            product.setCreatedAt(LocalDateTime.now());
            redisService.addItem(list.ticker(), new JsonBuilder().gson().toJson(product));
            return product;
        }).toList();
        productRepository.saveAll(mappedProduct);

    }

    @Transactional
    public Product getProductByTickerFromExchangeAndUpdate(String ticker){
        exchangeServerClient.setServer(ExchangeServer.MAL1);
        ProductDTO productDTO = exchangeServerClient.getProductTicker(ticker);
        exchangeServerClient.setServer(ExchangeServer.MAL2);
        ProductDTO productDTO2 = exchangeServerClient.getProductTicker(ticker);


        redisService.addItem(productDTO.ticker()+"_BID_PRICE_MAL1", productDTO.bidPrice());
        redisService.addItem(productDTO.ticker()+"_ASK_PRICE_MAL1", productDTO.askPrice());
        redisService.addItem(productDTO.ticker()+"_BID_PRICE_MAL2", productDTO2.bidPrice());
        redisService.addItem(productDTO.ticker()+"_ASK_PRICE_MAL2", productDTO2.askPrice());

        // Checking the two product and selecting the product with the highestPrice
        // This logic helps to show users the highestPrice in order for us to make profit when they decide to buy a product
        var preferredProduct = productDTO.lastTradedPrice() > productDTO2.lastTradedPrice() ? productDTO : productDTO2;

        Optional<Product> product = productRepository.findByTickerIgnoreCase(preferredProduct.ticker());

        if(product.isPresent()){
            var productToUpdate = product.get();
            productHistoryService.saveProductHistory(productToUpdate);
            productToUpdate.setAskPrice(preferredProduct.askPrice());
            productToUpdate.setBuyLimit(preferredProduct.buyLimit());
            productToUpdate.setBidPrice(preferredProduct.bidPrice());
            productToUpdate.setLastTradedPrice(preferredProduct.lastTradedPrice());
            productToUpdate.setMaxShiftPrice(preferredProduct.maxPriceShift());
            productToUpdate.setSellLimit(preferredProduct.sellLimit());
            return productRepository.save(productToUpdate);
        }

        Product newProduct = new Product();
        newProduct.setAskPrice(preferredProduct.askPrice());
        newProduct.setBuyLimit(preferredProduct.buyLimit());
        newProduct.setBidPrice(preferredProduct.bidPrice());
        newProduct.setTicker(preferredProduct.ticker());
        newProduct.setLastTradedPrice(preferredProduct.lastTradedPrice());
        newProduct.setMaxShiftPrice(preferredProduct.maxPriceShift());
        newProduct.setSellLimit(preferredProduct.sellLimit());
        newProduct.setTrading(true);
        productHistoryService.saveProductHistory(newProduct);
        return productRepository.save(newProduct);
    }

    public List<ProductDTO> getProductWithHighestPrice(List<ProductDTO> list1, List<ProductDTO> list2){
        List<ProductDTO> maxValues = new LinkedList<>();

        for (int i = 0; i < list1.size(); ++i)
        {
            // If item in list1 is larger, add it
            if (list1.get(i).lastTradedPrice() > list2.get(i).lastTradedPrice())
            {
                maxValues.add(list1.get(i));
            }
            else // else add the item from list2
            {
                maxValues.add(list2.get(i));
            }
        }

        return maxValues;
    }
}
