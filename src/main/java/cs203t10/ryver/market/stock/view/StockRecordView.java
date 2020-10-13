package cs203t10.ryver.market.stock.view;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.trade.Trade;
import lombok.*;

@Data @Builder
public class StockRecordView {

    private String symbol;

    private Double lastPrice;

    private Integer bidVolume;

    private Double bid;

    private Integer askVolume;

    private Double ask;

    public static StockRecordView fromRecordAskBid(StockRecord record) {
        return fromRecordAskBid(record, new Trade(), new Trade());
    }

    public static StockRecordView fromRecordAskBid(StockRecord record, Trade bestBuy, Trade bestSell) {
        return StockRecordView.builder()
                .symbol(record.getStock().getSymbol())
                .lastPrice(record.getPrice())
                .bidVolume(bestBuy.getQuantity())
                .bid(bestBuy.getPrice())
                .askVolume(bestSell.getQuantity())
                .ask(bestSell.getPrice())
                .build();
    }

}

