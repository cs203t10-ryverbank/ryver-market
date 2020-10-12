package cs203t10.ryver.market.trade.view;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.BeanUtils;

import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.Trade.Action;
import lombok.*;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class TradeView {

    private Integer id;

    private Action action;

    private String symbol;

    private Integer quantity;

    private Integer filledQuantity;

    private Integer customerId;

    private Integer accountId;

    private Double bid;

    private Double ask;

    private Double avgPrice;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date submittedDate;

    private Status status;

    public static TradeView fromTrade(Trade trade) {
        TradeView view = new TradeView();
        BeanUtils.copyProperties(trade, view);
        return view;
    }

}

