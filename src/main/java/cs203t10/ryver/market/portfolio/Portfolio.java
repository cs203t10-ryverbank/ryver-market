package cs203t10.ryver.market.portfolio;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Embedded;

import javax.validation.constraints.*;

import lombok.*;

import cs203t10.ryver.market.portfolio.Asset;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@ToString
public class Portfolio {

  @Id
  @NotNull(message = "Customer ID cannot be null")
  private Integer customerId;

  @Embedded
  private List<Asset> assets;
  
  @NotNull(message = "Unrealized gain/loss cannot be null")
  private Double unrealizedGainLoss;

  @NotNull(message = "Total gain/loss cannot be null")
  private Double totalGainLoss;
}