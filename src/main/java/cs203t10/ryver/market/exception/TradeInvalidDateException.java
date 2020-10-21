package cs203t10.ryver.market.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public  class TradeInvalidDateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TradeInvalidDateException(String time) {
        super(String.format("Current time is: %s. Please make a trade on weekdays, 9am to 5pm.", time));
    }

}