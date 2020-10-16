package cs203t10.ryver.market.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.Test;

import cs203t10.ryver.market.stock.view.StockRecordView;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;

public class StockRecordViewTest {

    Date firstDate = new Date(1602321010000L);

    Stock testStock = new Stock("A1");

    StockRecord testRecord = StockRecord.builder()
            .stock(testStock).submittedDate(firstDate)
            .price(1.0).totalVolume(1000).build();

    Trade testBuy = Trade.builder()
            .stock(testStock).action(Action.BUY)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate)
            .status(Status.OPEN).price(1.18).build();

    Trade testSell = Trade.builder()
            .stock(testStock).action(Action.SELL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate)
            .status(Status.OPEN).price(1.23).build();

    @Test
    public void stockRecordViewFromStockRecordTest_noTrades() {
        var result = StockRecordView.fromRecordAskBid(testRecord);
        StockRecordView expected = StockRecordView.builder()
                .symbol(testStock.getSymbol())
                .lastPrice(1.0)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void stockRecordViewFromStockRecordTest_nullRecord_throwError() {
        assertThrows(RuntimeException.class, () -> {
            StockRecordView.fromRecordAskBid(null);
        });
    }

    @Test
    public void stockRecordViewFromStockRecordTest_nullTrades() {
        var result = StockRecordView.fromRecordAskBid(testRecord, null, null);
        StockRecordView expected = StockRecordView.builder()
                .symbol(testStock.getSymbol())
                .lastPrice(testRecord.getPrice())
                .build();
        assertEquals(expected, result);
    }

    @Test
    public void stockRecordViewFromStockRecordTest() {
        var result = StockRecordView.fromRecordAskBid(testRecord, testBuy, testSell);
        StockRecordView expected = StockRecordView.builder()
                .symbol(testStock.getSymbol()).lastPrice(testRecord.getPrice())
                .bidVolume(testBuy.getQuantity()).bid(testBuy.getPrice())
                .askVolume(testSell.getQuantity()).ask(testSell.getPrice())
                .build();
        assertEquals(result, expected);
    }

}

