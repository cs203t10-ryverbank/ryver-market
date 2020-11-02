package cs203t10.ryver.market.trade;

import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonValue;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.trade.exception.*;

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

    @Builder.Default
    private Integer filledQuantity = 0;

    private Integer customerId;

    private Integer accountId;

    private Date submittedDate;

    @Builder.Default
    private Double availableBalance = 0.0;

    @AllArgsConstructor
    public enum Status {
        OPEN("open"),
        FILLED("filled"),
        PARTIAL_FILLED("partial-filled"),
        INVALID("invalid"),
        CANCELLED("cancelled"),
        EXPIRED("expired"),
        CLOSED("closed");

        @Getter @JsonValue
        private String status;
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.OPEN;

    @Builder.Default
    private Double totalPrice = 0.0;

    @Builder.Default
    private Double price = 0.0;
}

