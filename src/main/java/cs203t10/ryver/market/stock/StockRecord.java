package cs203t10.ryver.market.stock;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.*;

import lombok.*;

/**
 * A stock record in the market.
 * Each stock should have a bid volume and bid price, or an ask volume
 * and ask price, representing the best trade out of all trades for a
 * given stock.
 *
 * If the prices are equal, then the bid submitted earlier takes
 * precedence.
 */
@Entity
@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class StockRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "stock_id")
    private Stock stock;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedDate;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price cannot be less than 0")
    private Double price;

    @NotNull(message = "Total volume cannot be null")
    @Min(value = 0, message = "Total volume cannot be less than 0")
    private Integer totalVolume;

}

