package cs203t10.ryver.market.trade.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Trade not found.")
public class TradeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TradeNotFoundException(Integer id) {
        super(String.format("Trade with id: %s not found", id));
    }

    public TradeNotFoundException(String symbol) {
        super(String.format("Trade with symbol: %s not found", symbol));
    }

}

