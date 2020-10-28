package cs203t10.ryver.market.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Trade rejected, quantity must be multiple of 100.")
public class TradeForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TradeForbiddenException(Integer quantity) {
        super(String.format("Trade with quantity: %s must be multiple of 100", quantity));
    }

}

