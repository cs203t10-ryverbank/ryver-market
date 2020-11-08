package cs203t10.ryver.market.portfolio.asset;

import javax.persistence.*;

import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cs203t10.ryver.market.portfolio.Portfolio;
import lombok.*;

@Entity
@Data @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Asset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(targetEntity = Portfolio.class)
    @JoinColumn(name = "portfolio", nullable = false)
    private Portfolio portfolio;

    @NotNull(message = "Code cannot be null")
    private String code;

    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @NotNull(message = "Available quantity cannot be null")
    private Integer availableQuantity;

    public Double getAveragePrice() {
        return value / quantity;
    }

    @NotNull(message = "Value cannot be null")
    private Double value;

}

