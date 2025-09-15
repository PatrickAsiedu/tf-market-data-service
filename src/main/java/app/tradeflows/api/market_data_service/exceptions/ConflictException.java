package app.tradeflows.api.market_data_service.exceptions;

public class ConflictException extends Exception{
    public ConflictException(String message) {
        super(message);
    }
}
