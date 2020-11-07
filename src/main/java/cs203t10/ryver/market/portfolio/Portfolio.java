package cs203t10.ryver.market.portfolio;

import java.util.List;

import javax.persistence.*;

import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

import cs203t10.ryver.market.portfolio.asset.Asset;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Portfolio {

    @Id
    @NotNull(message = "Customer ID cannot be null")
    private Integer customerId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotNull(message = "Assets cannot be null")
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<Asset> assets;

    @NotNull(message = "Initial capital cannot be null")
    private Double initialCapital;

}

