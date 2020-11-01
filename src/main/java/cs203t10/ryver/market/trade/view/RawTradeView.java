package cs203t10.ryver.market.trade.view;

import java.util.Date;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.exception.*;
import lombok.*;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class RawTradeView {

    @AssertTrue(message = "Bid/ask price must be specified")
    private boolean isActionPriceSpecified() {
        return (action == Action.BUY && bid != null) || (action == Action.SELL && ask != null);
    }

    private Integer id;

    @NotNull(message="Action must be specified")
    private Action action;

    @NotNull(message="Symbol must be specified")
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

    @NotNull(message="Customer id must be specified")
    private Integer customerId;

    @NotNull(message="Account id must be specified")
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
    private Double bid;

    private Double ask;

    @Builder.Default
    private Double avgPrice = 0.0;


    /**
     * Converts a view into a Trade, without the Stock entity foreign key.
     */
    public TradeView toTradeView() {
        TradeView tradeView = TradeView.builder()
                                .id(id)
                                .action(action)
                                .symbol(symbol)
                                .quantity(quantity)
                                .filledQuantity(filledQuantity)
                                .customerId(customerId)
                                .accountId(accountId)
                                .submittedDate(submittedDate)
                                .status(status)
                                .bid(bid)
                                .ask(ask)
                                .avgPrice(avgPrice)
                                .build();
        return tradeView;
    }

}

