package cs203t10.ryver.market.portfolio.view;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import cs203t10.ryver.market.portfolio.asset.view.AssetInfoViewableByCustomer;
import lombok.*;

@Data
@AllArgsConstructor
public class PortfolioInfoViewableByCustomer {

  @JsonProperty("customer_id")
  private Integer customerId;

  @JsonProperty("assets")
  private List<AssetInfoViewableByCustomer> assets;

  @JsonProperty("unrealized_gain_loss")
  private Double unrealizedGainLoss;

  @JsonProperty("total_gain_loss")
  private Double totalGainLoss;
}