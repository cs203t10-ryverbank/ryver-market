package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.Test;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;

public class TradeViewTest {

    Date firstDate = new Date(1602321010000L);

    Stock testStock = new Stock("A1");

    Trade testBuy = Trade.builder()
            .stock(testStock).action(Action.BUY)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate)
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
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate)
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

}

