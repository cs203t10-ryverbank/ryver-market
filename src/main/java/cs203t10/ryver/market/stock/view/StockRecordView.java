package cs203t10.ryver.market.stock.view;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.trade.Ask;
import cs203t10.ryver.market.trade.Bid;
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
        return fromRecordAskBid(record, new Bid(), new Ask());
    }

    public static StockRecordView fromRecordAskBid(StockRecord record, Bid bid, Ask ask) {
        return StockRecordView.builder()
                .symbol(record.getStock().getSymbol())
                .lastPrice(record.getPrice())
                .bidVolume(bid.getVolume())
                .bid(bid.getPrice())
                .askVolume(ask.getVolume())
                .ask(ask.getPrice())
                .build();
    }

}

