package cs203t10.ryver.market.portfolio.asset;

import javax.validation.constraints.*;

import lombok.*;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@ToString
public class Asset {
  @NotNull(message = "Stock cannot be null")
  private Stock stock; 

  @NotNull(message = "Quantity cannot be null")
  private Integer quantity;

  @NotNull(message = "Average price cannot be null")
  private Double averagePrice;

  @NotNull(message = "Current price cannot be null")
  private Double currentPrice;

  @NotNull(message = "Value cannot be null")
  private Double value;

  @NotNull(message = "Gain/loss cannot be null")
  private Double gainLoss;
}
