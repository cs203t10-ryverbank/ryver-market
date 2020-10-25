package cs203t10.ryver.market.fund.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InsufficientBalanceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException(Integer accountId, Double amount, Double balance) {
        super(String.format(
                    "Account with id: %s has insufficient balance: withdrawing %s from %s",
                    accountId, amount, balance));
    }

}

