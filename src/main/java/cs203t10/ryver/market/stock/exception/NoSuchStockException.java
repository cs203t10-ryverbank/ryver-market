package cs203t10.ryver.market.stock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoSuchStockException(String symbol) {
        super(String.format("Stock with symbol: %s not found", symbol));
    }

}

