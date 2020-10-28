package cs203t10.ryver.market.trade.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class TradeNotAllowedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TradeNotAllowedException(final Integer tradeId, final Integer customerId) {
        super(String.format("Trade with id: %s was not made by customer with id: %s", tradeId, customerId));
    }

    public TradeNotAllowedException(final Integer postBodyCustomerId) {
        super(String.format("Trade must be made by customer with id: %s", postBodyCustomerId));
    }
}

