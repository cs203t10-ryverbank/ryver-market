package cs203t10.ryver.market.stock;

import java.util.Date;

import javax.validation.constraints.*;

import lombok.*;

/**
 * A stock in the market.
 * Each stock should have a bid volume and bid price, or an ask volume
 * and ask price, representing the best trade out of all trades for a
 * given stock.
 *
 * If the prices are equal, then the bid submitted earlier takes
 * precedence.
 */
@Data @Builder
public class Stock {

    @NotNull(message = "Symbol cannot be null")
    private String symbol;

    @NotNull(message = "Last price cannot be null")
    @Min(value = 0, message = "Last price cannot be less than 0")
    private Double lastPrice;

    private Integer totalVolume;

    private Integer bidVolume;

    private Double bid;

    private Integer askVolume;

    private Double ask;

    private Date submittedDate;

}

