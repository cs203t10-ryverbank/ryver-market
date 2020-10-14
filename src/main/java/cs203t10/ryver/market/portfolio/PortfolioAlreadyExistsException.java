package cs203t10.ryver.market.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class PortfolioAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID= 1L;

    public PortfolioAlreadyExistsException(Integer customerId) {
        super(String.format("Portfolio with customer id: %s already exists", customerId));
    }
}

