package cs203t10.ryver.market.portfolio;

import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import cs203t10.ryver.market.portfolio.asset.Asset;
import lombok.*;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode
public class PortfolioInitial {

    @Id
	@JsonProperty("customer_id")
	@NotNull(message = "Customer ID cannot be null")
	private Integer customerId;

	@JsonProperty("assets")
    @NotNull(message = "Assets cannot be null")
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<Asset> assets;
    
	@NotNull(message = "Total gain/loss cannot be null")
    private Double totalGainLoss;
    
    public Portfolio toPortfolio() {
        Portfolio portfolio = Portfolio.builder()
                            .customerId(customerId)
                            .assets(assets)
                            .totalGainLoss(totalGainLoss)
                            .build();
        return portfolio;
    }

}