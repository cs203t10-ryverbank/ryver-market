package cs203t10.ryver.market.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PortfolioException {

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Portfolio not found.")
  public static class PortfolioNotFoundException extends RuntimeException{
    private static final long serialVersionUID= 1L;

    public PortfolioNotFoundException(Integer customerId) {
      super(String.format("Portfolio with customer id: %s not found", customerId));
    }
  }
}