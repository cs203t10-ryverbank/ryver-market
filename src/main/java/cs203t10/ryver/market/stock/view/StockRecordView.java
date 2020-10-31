package cs203t10.ryver.market.stock.view;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.trade.Trade;

import lombok.*;

@Data @Builder
public class StockRecordView {

    private String symbol;

    private Double lastPrice;

    /**
     * Only show information for the best trade.
     *
     * The best trade is determined by:
     * 1. The price:
     *   - for buy trades, the higher the bid, the better;
     *   - for sell trades, the lower the ask, the better.
     * 2. The date submitted:
     *   - the earlier, the better.
     */
    @Builder.Default
    private Integer bidVolume = 0;

    @Builder.Default
    private Double bid = 0.0;

    @Builder.Default
    private Integer askVolume = 0;

    @Builder.Default
    private Double ask = 0.0;

    public static StockRecordView fromRecordAskBid(StockRecord record) {
        return fromRecordAskBid(record, 0.0, 0.0, 0, 0);
    }

    public static StockRecordView fromRecordAskBid(
            StockRecord record, Double bid, Double ask,
            Integer bidVolume, Integer askVolume) {

        if (record == null) {
            throw new RuntimeException("Cannot build stock record view from null record");
        }

        return StockRecordView.builder()
                .symbol(record.getStock().getSymbol())
                .lastPrice(record.getPrice())
                .bidVolume(bidVolume)
                .bid(bid)
                .askVolume(askVolume)
                .ask(ask)
                .build();
    }

}

