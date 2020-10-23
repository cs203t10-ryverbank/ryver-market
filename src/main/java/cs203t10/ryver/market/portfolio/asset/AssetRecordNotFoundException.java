package cs203t10.ryver.market.portfolio.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Portfolio not found.")
public class AssetRecordNotFoundException extends RuntimeException{
    private static final long serialVersionUID= 1L;

    public AssetRecordNotFoundException(Integer customerId, String code) {
    super(String.format("Customer id: %s does not own stock with symbol: %s", customerId, code));
    }
}