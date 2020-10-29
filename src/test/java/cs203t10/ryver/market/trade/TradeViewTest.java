package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import cs203t10.ryver.market.TestConstants;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;

public class TradeViewTest {

    Stock testStock = new Stock("A1");

    Trade testBuy = Trade.builder()
            .stock(testStock).action(Action.BUY)
            .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
            .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
            .submittedDate(TestConstants.FIRST_DATE)
            .status(Status.OPEN).price(1.18).build();

    TradeView testBuyView = TradeView.builder()
            .action(testBuy.getAction())
            .symbol(testStock.getSymbol())
            .quantity(testBuy.getQuantity())
            .filledQuantity(testBuy.getFilledQuantity())
            .customerId(testBuy.getCustomerId())
            .accountId(testBuy.getAccountId())
            .submittedDate(testBuy.getSubmittedDate())
            .status(testBuy.getStatus())
            .bid(testBuy.getPrice())
            .avgPrice(0.0)
            .build();

    Trade testSell = Trade.builder()
            .stock(testStock).action(Action.SELL)
            .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
            .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
            .submittedDate(TestConstants.FIRST_DATE)
            .status(Status.OPEN).price(1.23).build();

    TradeView testSellView = TradeView.builder()
            .action(testSell.getAction())
            .symbol(testStock.getSymbol())
            .quantity(testSell.getQuantity())
            .filledQuantity(testSell.getFilledQuantity())
            .customerId(testSell.getCustomerId())
            .accountId(testSell.getAccountId())
            .submittedDate(testSell.getSubmittedDate())
            .status(testSell.getStatus())
            .ask(testSell.getPrice())
            .avgPrice(0.0)
            .build();

    @Test
    public void fromBuyTradeTest() {
        var result = TradeView.fromTrade(testBuy);
        assertEquals(testBuyView, result);
    }

    @Test
    public void fromSellTradeTest() {
        var result = TradeView.fromTrade(testSell);
        assertEquals(testSellView, result);
    }

    @Test
    public void fromTradeTest_nullTrade_throws() {
        assertThrows(RuntimeException.class, () -> {
            TradeView.fromTrade(null);
        });
    }

    @Test
    public void toBuyTradeTest() {
        var result = testBuyView.toTrade();
        var expected = testBuy.toBuilder().stock(null).build();
        assertEquals(expected, result);
    }

    @Test
    public void toSellTradeTest() {
        var result = testSellView.toTrade();
        var expected = testSell.toBuilder().stock(null).build();
        assertEquals(expected, result);
    }

    @Test
    public void tradeView_AveragePrice() {
        Trade trade= Trade.builder()
            .action(Action.BUY).stock(TestConstants.STOCK)
            .quantity(TestConstants.BUY_QUANTITY)
            .filledQuantity(TestConstants.PARTIAL_QUANTITY)
            .customerId(TestConstants.CUSTOMER_ID)
            .accountId(TestConstants.ACCOUNT_ID)
            .submittedDate(TestConstants.FIRST_DATE)
            .status(Status.PARTIAL_FILLED)
            .totalPrice(20000.0)
            .build();

        TradeView tradeView = TradeView.fromTrade(trade);
        Double expectedAveragePrice = 4.0;
        Double actualAveragePrice = tradeView.getAvgPrice();
        assertEquals(expectedAveragePrice, actualAveragePrice);
    }
}

