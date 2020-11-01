package cs203t10.ryver.market.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import cs203t10.ryver.market.TestConstants;
import cs203t10.ryver.market.stock.view.StockRecordView;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;

public class StockRecordViewTest {


    // StockRecord testRecord = StockRecord.builder()
    //         .stock(TestConstants.STOCK).submittedDate(TestConstants.FIRST_DATE)
    //         .price(1.0).totalVolume(1000).build();

    // Trade testBuy = Trade.builder()
    //         .stock(TestConstants.STOCK).action(Action.BUY)
    //         .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
    //         .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
    //         .submittedDate(TestConstants.FIRST_DATE)
    //         .status(Status.OPEN).price(1.18).build();

    // Trade testSell = Trade.builder()
    //         .stock(TestConstants.STOCK).action(Action.SELL)
    //         .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
    //         .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
    //         .submittedDate(TestConstants.FIRST_DATE)
    //         .status(Status.OPEN).price(1.23).build();

    // @Test
    // public void stockRecordViewFromStockRecordTest_noTrades() {
    //     var result = StockRecordView.fromRecordAskBid(testRecord);
    //     StockRecordView expected = StockRecordView.builder()
    //             .symbol(TestConstants.SYMBOL)
    //             .ask(testRecord.getPrice())
    //             .bid(testRecord.getPrice())
    //             .lastPrice(1.0)
    //             .build();

    //     assertEquals(expected, result);
    // }

    // @Test
    // public void stockRecordViewFromStockRecordTest_nullRecord_throwError() {
    //     assertThrows(RuntimeException.class, () -> {
    //         StockRecordView.fromRecordAskBid(null);
    //     });
    // }

    // @Test
    // public void stockRecordViewFromStockRecordTest_nullTrades() {
    //     var result = StockRecordView.fromRecordAskBid(testRecord, null, null, 0, 0);
    //     StockRecordView expected = StockRecordView.builder()
    //             .symbol(TestConstants.SYMBOL)
    //             .bid(testRecord.getPrice())
    //             .ask(testRecord.getPrice())
    //             .lastPrice(testRecord.getPrice())
    //             .build();
    //     assertEquals(expected, result);
    // }

    // @Test
    // public void stockRecordViewFromStockRecordTest() {
    //     var result = StockRecordView.fromRecordAskBid(testRecord, testBuy, testSell, 10000, 10000);
    //     StockRecordView expected = StockRecordView.builder()
    //             .symbol(TestConstants.SYMBOL).lastPrice(testRecord.getPrice())
    //             .bidVolume(TestConstants.BUY_QUANTITY).bid(testBuy.getPrice())
    //             .askVolume(TestConstants.SELL_QUANTITY).ask(testSell.getPrice())
    //             .build();
    //     assertEquals(expected, result);
    // }

}

