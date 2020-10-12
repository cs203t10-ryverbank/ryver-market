package cs203t10.ryver.market.stock.view;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.trade.BuyTrade;
import cs203t10.ryver.market.trade.SellTrade;
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
        return fromRecordAskBid(record, new BuyTrade(), new SellTrade());
    }

    public static StockRecordView fromRecordAskBid(StockRecord record, BuyTrade buy, SellTrade sell) {
        return StockRecordView.builder()
                .symbol(record.getStock().getSymbol())
                .lastPrice(record.getPrice())
                .bidVolume(buy.getVolume())
                .bid(buy.getBidPrice())
                .askVolume(sell.getVolume())
                .ask(sell.getAskPrice())
                .build();
    }

}

