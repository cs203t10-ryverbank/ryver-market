package cs203t10.ryver.market.portfolio.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientStockQuantityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientStockQuantityException (Integer customerId, String code) {
        super(String.format("Customer with id: %s does not own a large enough quantity of the stock with symbol: %s for this action", customerId, code));
    }

}