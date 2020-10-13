package cs203t10.ryver.market.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TradeException {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Stock symbol is invalid.")
    public static class StockSymbolInvalidException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public StockSymbolInvalidException(String symbol) {
            super(String.format("Stock with symbol %s does not exist", symbol));
        }

    }
    
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Customer ID does not belong to you.")
    public static class CustomerNoAccessException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CustomerNoAccessException(Integer customerId) {
            super(String.format("Customer with id: %s does not match the logged in user.", customerId));
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Trade not found.")
    public static class TradeNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TradeNotFoundException(Integer id) {
            super(String.format("Trade with id: %s not found", id));
        }

    }



}

