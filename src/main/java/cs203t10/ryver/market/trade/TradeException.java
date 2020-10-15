package cs203t10.ryver.market.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TradeException {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Trade not found.")
    public static class TradeNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TradeNotFoundException(Integer id) {
            super(String.format("Trade with id: %s not found", id));
        }

        public TradeNotFoundException(String symbol) {
            super(String.format("Trade with symbol: %s not found", symbol));
        }

    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Trade rejected, quantity must be multiple of 100.")
    public static class TradeForbiddenException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TradeForbiddenException(Integer quantity) {
            super(String.format("Trade with quantity: %s must be multiple of 100", quantity));
        }
    }

}

