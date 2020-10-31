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

    @Builder.Default
    private Integer quantity = 0;

    @Builder.Default
    private Integer filledQuantity = 0;

    private Integer customerId;

    private Integer accountId;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date submittedDate;

    private Status status;

    /**
     * The better-priced trade will be matched first.
     *
     * If prices are the same, earlier trade will be matched first
     */
    @Builder.Default
    private Double bid = 0.0;

    @Builder.Default
    private Double ask = 0.0;

    @Builder.Default
    private Double avgPrice = 0.0;

    public static TradeView fromTrade(Trade trade) {
        if (trade == null) {
            throw new RuntimeException("Cannot build trade view from null trade");
        }
        TradeView view = new TradeView();
        BeanUtils.copyProperties(trade, view);
        // Set the symbol for the trade view
        view.setSymbol(trade.getStock().getSymbol());
        // Set the bid or ask of the trade view
        if (trade.getAction().equals(Action.BUY)) {
            view.setBid(trade.getPrice());
        } else {
            view.setAsk(trade.getPrice());
        }
        // Set the average price of the trade view
        if (trade.getFilledQuantity() != 0) {
            view.setAvgPrice(trade.getTotalPrice() / trade.getFilledQuantity());
        }
        // Return partial-filled
        if (trade.getStatus() == Status.INVALID){
            view.setStatus(Status.PARTIAL_FILLED);
        }
        return view;
    }

    /**
     * Converts a view into a Trade, without the Stock entity foreign key.
     */
    public Trade toTrade() {
        Trade trade = new Trade();
        BeanUtils.copyProperties(this, trade);
        // Set the price of the trade
        trade.setPrice(action.equals(Action.BUY) ? bid : ask);
        // Set the total price of the trade
        trade.setTotalPrice(avgPrice * filledQuantity);
        return trade;
    }

}

