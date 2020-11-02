package cs203t10.ryver.market.trade.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TradeForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TradeForbiddenException(Integer quantity) {
        super(String.format("Trade with quantity: %s must be multiple of 100", quantity));
    }

    public TradeForbiddenException(String symbol) {
        super(String.format("No such stock : %s", symbol));
    }

    public TradeForbiddenException(String symbol, Integer quantity) {
        super(String.format("Not enough stock : %s to sell. User does not have %s stocks", symbol, quantity));
    }

}

