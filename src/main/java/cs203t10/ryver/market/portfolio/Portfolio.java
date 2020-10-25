package cs203t10.ryver.market.portfolio;

import java.util.List;

import javax.persistence.*;

import javax.validation.constraints.*;

import lombok.*;

import cs203t10.ryver.market.portfolio.asset.Asset;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode
public class Portfolio {

    @Id
    @NotNull(message = "Customer ID cannot be null")
    private Integer customerId;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<Asset> assets;

    @NotNull(message = "Unrealized gain/loss cannot be null")
    private Double unrealizedGainLoss;

    @NotNull(message = "Total gain/loss cannot be null")
    private Double totalGainLoss;
}
