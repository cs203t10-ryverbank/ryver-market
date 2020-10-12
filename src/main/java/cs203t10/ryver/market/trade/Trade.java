package cs203t10.ryver.market.trade;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonValue;

import cs203t10.ryver.market.stock.Stock;

import lombok.*;

@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
public class Trade {

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

    private Integer quantity;

    private Integer filledQuantity;

    private Integer customerId;

    private Integer accountId;

    private Date submittedDate;

    private Double price;

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

}

