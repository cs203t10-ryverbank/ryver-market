package cs203t10.ryver.market.portfolio.asset;

import javax.persistence.*;

import javax.validation.constraints.*;

import cs203t10.ryver.market.portfolio.Portfolio;

import lombok.*;

@Entity
@Getter @Setter @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode
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

    @NotNull(message = "Average price cannot be null")
    private Double averagePrice;

    @NotNull(message = "Value cannot be null")
    private Double value;

}

