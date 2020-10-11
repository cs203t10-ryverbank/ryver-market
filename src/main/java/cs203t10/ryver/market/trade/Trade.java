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

import cs203t10.ryver.market.stock.Stock;

import lombok.*;

@Entity
@Data
public abstract class Trade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "stock_id")
    private Stock stock;

    private Integer customerId;

    private Integer accountId;

    private Date submittedDate;

    public enum Type {
        ASK, BID;
    }

    @Enumerated(EnumType.STRING)
    @Setter(value = AccessLevel.NONE)
    private Type type;

    private Integer volume;

    private Integer filledVolume;

    private Double price;

    public enum Status {
        OPEN,
        FILLED,
        PARTIAL_FILLED,
        CANCELLED,
        EXPIRED,
    }

    @Enumerated(EnumType.STRING)
    private Status status;

}

