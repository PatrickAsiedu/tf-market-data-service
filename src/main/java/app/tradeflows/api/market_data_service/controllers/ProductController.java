package app.tradeflows.api.market_data_service.controllers;

import app.tradeflows.api.market_data_service.entities.Product;
import app.tradeflows.api.market_data_service.exceptions.NotFoundException;
import app.tradeflows.api.market_data_service.services.ProductService;
import app.tradeflows.api.market_data_service.config.SocketConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/products")
public class ProductController {


    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final SocketConnectionHandler socketConnectionHandler;

    public ProductController(ProductService productService, SocketConnectionHandler socketConnectionHandler) {
        this.productService = productService;
        this.socketConnectionHandler = socketConnectionHandler;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Product>> getProducts(){
        List<Product> products = productService.getAllProduct();
        return ResponseEntity.ok(products);
    }

    @GetMapping(value = "/{ticker}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> getProductByTicker(@PathVariable String ticker) throws NotFoundException {
        Product product = productService.getProductByTicker(ticker);
        return ResponseEntity.ok(product);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> getProductById(@PathVariable String id) throws NotFoundException {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }


}
