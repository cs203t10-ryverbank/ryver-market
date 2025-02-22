package cs203t10.ryver.market.fund.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException(Integer accountId, Double amount) {
        super(String.format(
                    "Account with id: %s has insufficient balance to withdraw %s",
                    accountId, amount));
    }

}

