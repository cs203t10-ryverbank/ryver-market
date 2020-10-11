package cs203t10.ryver.market.stock;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import lombok.*;

@Entity
@Data @Builder
public class Stock {

    @Id @NotNull(message = "Symbol cannot be null")
    private String symbol;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<StockRecord> records;

}

