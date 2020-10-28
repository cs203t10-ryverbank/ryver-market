package cs203t10.ryver.market.fund.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccountNotAllowedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AccountNotAllowedException(Integer customerId, Integer accountId) {
        super(String.format("Account with id: %s not owned by customer with id: %s", accountId, customerId));
    }

}

