package app.tradeflows.api.market_data_service.controllers;

import app.tradeflows.api.market_data_service.dto.WebhookDTO;
import app.tradeflows.api.market_data_service.services.SubscriptionService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/subscription")
@CrossOrigin
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    private final SubscriptionService webHookService;

    public SubscriptionController(SubscriptionService webHookService){
        this.webHookService = webHookService;
    }

    @PostMapping(value = "/updates", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> updates(@RequestBody WebhookDTO webhookDTO, HttpServletRequest request){

        logger.info("request - Remoted Addr: {}", request.getRemoteAddr());
        logger.info("request - Remoted Host: {}", request.getRemoteHost());
        logger.info("Received webhook payload: {}", webhookDTO.toString());
        webHookService.handleRequest(webhookDTO);
        return ResponseEntity.accepted().build();
    }
}
