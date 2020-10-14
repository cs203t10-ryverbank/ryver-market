package cs203t10.ryver.market.stock;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import cs203t10.ryver.market.trade.Trade;
import lombok.*;

@Entity
@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class Stock {

    @Id @NotNull(message = "Symbol cannot be null")
    private String symbol;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private List<StockRecord> records;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private List<Trade> trades;

}

