package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cs203t10.ryver.market.TestConstants;
import cs203t10.ryver.market.trade.ReconciliationService.PriceQuantityTrades;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.exception.NoTradesToReconcile;

@ExtendWith(MockitoExtension.class)
public class ReconciliationServiceTest {

    @Mock
    private TradeService tradeService;

    @InjectMocks
    private ReconciliationService reconService;

    Trade marketBuy = Trade.builder()
        .stock(TestConstants.STOCK).action(Action.BUY)
        .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
        .availableBalance(TestConstants.MARKET_PRICE * TestConstants.BUY_QUANTITY)
        .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
        .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
        .price(0.0).build();

    Trade marketBuyPartial = marketBuy.toBuilder()
        .filledQuantity(TestConstants.PARTIAL_QUANTITY)
        .availableBalance(TestConstants.MARKET_PRICE * (TestConstants.BUY_QUANTITY - TestConstants.PARTIAL_QUANTITY))
        .status(Status.PARTIAL_FILLED)
        .build();

    Trade marketSell = Trade.builder()
        .stock(TestConstants.STOCK).action(Action.SELL)
        .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
        .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
        .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
        .price(0.0).build();

    Trade marketSellPartial = marketSell.toBuilder()
        .filledQuantity(TestConstants.PARTIAL_QUANTITY)
        .status(Status.PARTIAL_FILLED)
        .build();

    Trade limitBuyAboveMarket = marketBuy.toBuilder()
        .availableBalance(TestConstants.HIGH_PRICE * TestConstants.BUY_QUANTITY)
        .price(TestConstants.HIGH_PRICE).build();

    Trade limitSellAboveMarket = marketSell.toBuilder()
        .price(TestConstants.HIGH_PRICE).build();

    Trade limitBuyBelowMarket = marketBuy.toBuilder()
        .availableBalance(TestConstants.LOW_PRICE * TestConstants.BUY_QUANTITY)
        .price(TestConstants.LOW_PRICE).build();

    Trade limitSellBelowMarket = marketSell.toBuilder()
        .price(TestConstants.LOW_PRICE).build();

    @Test
    public void getTransactedPriceQuantity_notEnoughAvailableBalanceOnMarketBuy() {
        Status expectedBuyStatus = Status.INVALID;
        Integer expectedQuantity = TestConstants.BUY_QUANTITY_MARKET_TO_HIGH_PRICE_NOT_ENOUGH;
        Double expectedPrice = TestConstants.HIGH_PRICE;

        PriceQuantityTrades result = reconService.getTransactedPriceQuantity(limitSellAboveMarket, marketBuy);

        assertEquals(expectedBuyStatus, result.getBestBuy().getStatus());
        assertEquals(expectedQuantity, result.getQuantity());
        assertEquals(expectedPrice, result.getPrice());
    }

    @Test
    public void getTranscatedPrice_marketSellAndBuy() {
        when(tradeService.determineTransactedPriceIfBothMarketOrders(marketSell, marketBuy))
            .thenReturn(TestConstants.MARKET_PRICE);

        Double result = reconService.getTransactedPrice(marketSell, marketBuy);
        assertEquals(TestConstants.MARKET_PRICE, result);
    }

    @Test
    public void getTranscatedPrice_marketSellLimitBuy() {
        Double result = reconService.getTransactedPrice(marketSell, limitBuyBelowMarket);
        assertEquals(TestConstants.LOW_PRICE, result);
    }

    @Test
    public void getTranscatedPrice_limitSellMarketBuy() {
        Double result = reconService.getTransactedPrice(limitSellAboveMarket, marketBuy);
        assertEquals(TestConstants.HIGH_PRICE, result);
    }

    @Test
    public void getTranscatedPrice_limitSellLimitBuy_sellLowerThanBuy() {
        when(tradeService.determineTransactedPriceIfBothLimitOrders(limitSellBelowMarket, limitBuyAboveMarket))
            .thenReturn(TestConstants.HIGH_PRICE);
        when(tradeService.determineTransactedPriceIfBothLimitOrders(limitSellBelowMarket, limitBuyBelowMarket))
            .thenReturn(TestConstants.HIGH_PRICE);

        Double result = reconService.getTransactedPrice(limitSellBelowMarket, limitBuyAboveMarket);
        assertEquals(TestConstants.HIGH_PRICE, result);

        Double equalPriceResult = reconService.getTransactedPrice(limitSellBelowMarket, limitBuyBelowMarket);
        assertEquals(TestConstants.HIGH_PRICE, equalPriceResult);
    }

    @Test
    public void getTransactedPrice_limitSellLimitBuy_buyLowerThanSell() {
        assertThrows(NoTradesToReconcile.class, () -> {
            reconService.getTransactedPrice(limitSellAboveMarket, limitBuyBelowMarket);
        });
    }

    @Test
    public void getTransactedQuantity_sellQuantityLow() {
        int expected = marketSellPartial.getQuantity() - marketSellPartial.getFilledQuantity();
        Integer result = reconService.getTransactedQuantity(marketSellPartial, marketBuy);
        assertEquals(expected, result);
    }

    @Test
    public void getTransactedQuantity_buyQuantityLow() {
        int expected = marketBuyPartial.getQuantity() - marketBuyPartial.getFilledQuantity();
        Integer result = reconService.getTransactedQuantity(marketSell, marketBuyPartial);
        assertEquals(expected, result);
    }

}

