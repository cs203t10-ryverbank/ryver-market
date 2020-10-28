package cs203t10.ryver.market.portfolio.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class StockNotOwnedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StockNotOwnedException(Integer customerId, String code) {
        super(String.format("Customer with id: %s does not own any stocks with symbol: %s", customerId, code));
    }

}

