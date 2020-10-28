package cs203t10.ryver.market.portfolio.asset.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@AllArgsConstructor
public class AssetInfoViewableByCustomer {

  @JsonProperty("code")
  private String code;

  @JsonProperty("quantity")
  private Integer quantity;

  @JsonProperty("avg_price")
  private Double averagePrice;

  @JsonProperty("current_price")
  private Double currentPrice;

  @JsonProperty("value")
  private Double value;

  @JsonProperty("gain_loss")
  private Double gainLoss;
}