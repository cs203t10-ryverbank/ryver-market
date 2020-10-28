package cs203t10.ryver.market.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public final class TradeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TradeNotFoundException(final Integer id) {
        super(String.format("Trade with id: %s not found", id));
    }

}

