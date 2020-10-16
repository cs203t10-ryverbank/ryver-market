package cs203t10.ryver.market.trade;

import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonValue;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.trade.TradeException.TradeForbiddenException;

import lombok.*;

@Entity
@Data @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
public class Trade {

    public Trade(Stock stock) {
        this();
        this.stock = stock;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @AllArgsConstructor
    public enum Action {
        BUY("buy"), SELL("sell");

        @Getter @JsonValue
        private String action;
    }

    @Enumerated(EnumType.STRING)
    private Action action;

    @ManyToOne @JoinColumn(name = "stock_id")
    private Stock stock;

    @Builder.Default
    private Integer quantity = 0;

    public Integer getQuantity() {
        if (quantity % 100 != 0)
            throw new TradeForbiddenException(quantity);
        else
            return quantity;
    }

    @Builder.Default
    private Integer filledQuantity = 0;

    private Integer customerId;

    private Integer accountId;

    private Date submittedDate;

    @AllArgsConstructor
    public enum Status {
        OPEN("open"),
        FILLED("filled"),
        PARTIAL_FILLED("partial-filled"),
        CANCELLED("cancelled"),
        EXPIRED("expired");

        @Getter @JsonValue
        private String status;
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private Double totalPrice = 0.0;

    @Builder.Default
    private Double price = 0.0;
}

