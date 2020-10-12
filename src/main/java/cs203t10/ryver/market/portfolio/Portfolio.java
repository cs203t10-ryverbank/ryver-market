package cs203t10.ryver.market.portfolio;

import javax.validation.constraints.*;

import lombok.*;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@ToString
public class Portfolio {

  @NotNull(message = "Customer ID cannot be null")
  private Integer customerId;

  private List<Asset> assets;
  
  @NotNull(message = "Unrealized gain/loss cannot be null")
  private Double unrealizedGainLoss;

  @NotNull(message = "Total gain/loss cannot be null")
  private Double totalGainLoss;
}