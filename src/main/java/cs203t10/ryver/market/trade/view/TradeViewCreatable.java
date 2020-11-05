package cs203t10.ryver.market.trade.view;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.BeanUtils;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.exception.*;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;

@Hidden
@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class TradeViewCreatable {

    private Integer id;

    private Action action;

    private String symbol;

    @Builder.Default
    private Integer quantity = 0;

    private static final int TRADE_MIN_RES = 100;
    public Integer getQuantity() {
        if (quantity % TRADE_MIN_RES != 0) {
            throw new TradeForbiddenException(quantity);
        } else {
            return quantity;
        }
    }

    @Builder.Default
    private Integer filledQuantity = 0;

    private Integer customerId;

    private Integer accountId;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date submittedDate;

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

    public static TradeViewCreatable fromTrade(Trade trade) {
        if (trade == null) {
            throw new RuntimeException("Cannot build trade view from null trade");
        }
        TradeViewCreatable view = new TradeViewCreatable();
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
        return view;
    }

    /**
     * Converts a view into a Trade, without the Stock entity foreign key.
     *
     * The status of the trade is OPEN by default.
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

