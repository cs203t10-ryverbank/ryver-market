package cs203t10.ryver.market.portfolio;

import javax.persistence.Embeddable;

import javax.validation.constraints.*;

import lombok.*;

@Embeddable
@Data @Builder(toBuilder = true)
public class Asset {

    @NotNull(message = "Code cannot be null")
    private String code;

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

