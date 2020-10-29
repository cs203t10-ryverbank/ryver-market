package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import cs203t10.ryver.market.TestConstants;
import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.fund.exception.*;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.exception.TradeNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    TradeRepository tradeRepo;

    @Mock
    FundTransferService fundTransferService;

    @Mock
    StockRecordService stockRecordService;

    @InjectMocks
    TradeServiceImpl tradeService;

    TradeView marketMakerBuy = TradeView.builder()
        .action(Action.BUY).symbol(TestConstants.STOCK.getSymbol())
        .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
        .customerId(0).accountId(0)
        .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
        .bid(TestConstants.LOW_PRICE).avgPrice(0.0).build();

    Trade marketBuy = Trade.builder()
        .stock(TestConstants.STOCK).action(Action.BUY)
        .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
        .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
        .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
        .price(0.0).build();

    Trade marketSell = Trade.builder()
        .stock(TestConstants.STOCK).action(Action.SELL)
        .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
        .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
        .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
        .price(0.0).build();

    @Test
    public void getBestBuy_BetterMarketBuy_ReturnMarketBuy(){
        Trade limitBuy = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.LOW_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();


        tradeRepo.saveAll(List.of(marketBuy, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketBuyBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(marketBuy));
        when(tradeRepo.findBestLimitBuyBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestBuy(TestConstants.SYMBOL);
        Trade expected = marketBuy;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketBuyBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(TestConstants.SYMBOL);
    }

    @Test
    public void getBestBuy_BetterLimitBuy_ReturnLimitBuy(){
        Trade limitBuy = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();


        tradeRepo.saveAll(List.of(marketBuy, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketBuyBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(marketBuy));
        when(tradeRepo.findBestLimitBuyBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestBuy(TestConstants.SYMBOL);
        Trade expected = limitBuy;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketBuyBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(TestConstants.SYMBOL);
    }

    @Test
    public void getBestSell_BetterMarketSell_ReturnMarketSell(){
        Trade limitBuy = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.LOW_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();


        tradeRepo.saveAll(List.of(marketSell, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketSellBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(marketSell));
        when(tradeRepo.findBestLimitBuyBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestSell(TestConstants.SYMBOL);
        Trade expected = marketSell;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketSellBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(TestConstants.SYMBOL);
    }

    @Test
    public void getBestSell_BetterLimitSell_ReturnLimitSell(){
        Trade limitBuy = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.SELL_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.LOW_PRICE).build();


        tradeRepo.saveAll(List.of(marketSell, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketSellBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(marketSell));
        when(tradeRepo.findBestLimitBuyBySymbol(TestConstants.SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestSell(TestConstants.SYMBOL);
        Trade expected = limitSell;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketSellBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(TestConstants.SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(TestConstants.SYMBOL);
    }

    @Test
    public void getTotalBidVolume_IgnoreExpiredCancelledFilled_ReturnValid(){
        Trade trade1 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();

        Trade trade2 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.PARTIAL_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.PARTIAL_FILLED)
                .price(TestConstants.LOW_PRICE).build();

        Trade trade3 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.PARTIAL_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.EXPIRED)
                .price(TestConstants.LOW_PRICE).build();

        Trade trade4 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.PARTIAL_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.CANCELLED)
                .price(TestConstants.LOW_PRICE).build();

        Trade trade5 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.BUY_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.FILLED)
                .price(TestConstants.LOW_PRICE).build();

        List trades = List.of(trade1, trade2, trade3, trade4, trade5);
        tradeRepo.saveAll(trades);

        when(tradeRepo.findAllBuyTradesBySymbol(TestConstants.SYMBOL)).thenReturn(trades);

        Integer expected = 15000;
        Integer actual = tradeService.getTotalBidVolume(TestConstants.SYMBOL);
        assertEquals(expected, actual);
        verify(tradeRepo).findAllBuyTradesBySymbol(TestConstants.SYMBOL);
    }

    @Test
    public void getTotalAskVolume_IgnoreExpiredCancelledFilled_ReturnValid(){
        Trade trade1 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.HIGH_PRICE).build();

        Trade trade2 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.PARTIAL_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.PARTIAL_FILLED)
                .price(TestConstants.LOW_PRICE).build();

        Trade trade3 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.PARTIAL_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.EXPIRED)
                .price(TestConstants.LOW_PRICE).build();

        Trade trade4 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.PARTIAL_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.CANCELLED)
                .price(TestConstants.LOW_PRICE).build();

        Trade trade5 = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.SELL)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(TestConstants.BUY_QUANTITY)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.FILLED)
                .price(TestConstants.LOW_PRICE).build();

        List trades = List.of(trade1, trade2, trade3, trade4, trade5);
        tradeRepo.saveAll(trades);

        when(tradeRepo.findAllSellTradesBySymbol(TestConstants.SYMBOL)).thenReturn(trades);

        Integer expected = 15000;
        Integer actual = tradeService.getTotalAskVolume(TestConstants.SYMBOL);
        assertEquals(expected, actual);
        verify(tradeRepo).findAllSellTradesBySymbol(TestConstants.SYMBOL);
    }


    // public void getBestBuy_DifferentPrice_ReturnHigherBid(){

    // }

    // public void getBestSell_DifferentPrice_ReturnLowerBid(){

    // }

    // public void getBestSell_SamePrice_ReturnEarlierBid(){

    // }

    // Why does this make marketMakerBuy a nullpointer?
    @Test
    public void registerBuyTradeTest_marketMaker_noRegister() {
        tradeService.saveTrade(marketMakerBuy);

        // Ensure that the fund transfer service is not called.
        verifyNoInteractions(fundTransferService);
        verify(tradeRepo).saveWithSymbol(
            marketMakerBuy.toTrade(),
            marketMakerBuy.getSymbol());
    }

    @Test
    public void getTradeTest_nonExistentTrade() {
        when(tradeRepo.findById(TestConstants.NOT_EXISTS_ID))
            .thenReturn(Optional.empty());

        assertThrows(TradeNotFoundException.class, () -> {
            tradeService.getTrade(TestConstants.NOT_EXISTS_ID);
        });
    }

    @Test
    public void saveTrade_AccountDoesNotBelongToCustomer_noRegister() {
        TradeView testBuy = TradeView.builder()
            .action(Action.BUY).symbol(TestConstants.SYMBOL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(50)
            .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
            .bid(2.0).avgPrice(0.0).build();

        Mockito.doThrow(new AccountNotAllowedException(1, 50))
                .when(fundTransferService)
                .deductAvailableBalance(any(Integer.class), any(Integer.class), any(Double.class));

        assertThrows(AccountNotAllowedException.class, () -> {
            tradeService.saveTrade(testBuy);
        });
        verifyNoInteractions(tradeRepo);
    }

    @Test
    public void saveTrade_InsufficientBalance_noRegister() {
        TradeView testBuy = TradeView.builder()
            .action(Action.BUY).symbol(TestConstants.SYMBOL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
            .bid(2.0).avgPrice(0.0).build();

        Mockito.doThrow(new InsufficientBalanceException(1, 20000.0, 0.0))
                .when(fundTransferService)
                .deductAvailableBalance(any(Integer.class), any(Integer.class), any(Double.class));

        assertThrows(InsufficientBalanceException.class, () -> {
            tradeService.saveTrade(testBuy);
        });
        verifyNoInteractions(tradeRepo);
    }

    // @Test
    // public void saveTrade_ValidTrade_Register() {
    //     TradeView testBuy = TradeView.builder()
    //         .action(Action.BUY).symbol(SYMBOL)
    //         .quantity(10000).filledQuantity(0)
    //         .customerId(1).accountId(1)
    //         .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
    //         .bid(2.0).avgPrice(0.0).build();

    //     Mockito.doNothing()
    //         .when(fundTransferService)
    //         .deductAvailableBalance(any(Integer.class), any(Integer.class), any(Double.class));

    //     when(tradeRepo.saveWithSymbol(testBuy.toTrade(), testBuy.getSymbol()))
    //         .thenReturn(testBuy.toTrade());

    //     Trade testTrade = tradeService.saveTrade(testBuy);
    //     assertEquals(testBuy.toTrade(), testTrade);

    //     verify(fundTransferService).deductAvailableBalance(1, 1, 20000.0);

    //     verify(tradeRepo).saveWithSymbol(testBuy.toTrade(), testBuy.getSymbol());
    // }

    @Test
    public void reconcileMarket_BetterMarketBuy_MatchMarketOrder(){
        // check partial filled
        // check Avg price
        // do for both buys and sells

        Trade testBuy = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.LOW_PRICE).build();

        Trade testBought = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(2000).filledQuantity(2000)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.FILLED)
                .price(TestConstants.HIGH_PRICE).build();

        Trade testSell = Trade.builder()
                .stock(TestConstants.STOCK).action(Action.BUY)
                .quantity(TestConstants.PARTIAL_QUANTITY).filledQuantity(0)
                .customerId(TestConstants.CUSTOMER_ID).accountId(TestConstants.ACCOUNT_ID)
                .submittedDate(TestConstants.FIRST_DATE).status(Status.OPEN)
                .price(TestConstants.LOW_PRICE).build();

        Double transactedPrice = 0.0;
        if (testBought.getPrice() == 0 && testBuy.getPrice() == 0) {
                // Get last price if there are no prices available.
                StockRecord latestStock
                        = stockRecordService.getLatestStockRecordBySymbol(TestConstants.SYMBOL);
                transactedPrice = latestStock.getPrice();
        } else if (testBought.getPrice() == 0) {
                transactedPrice = testBuy.getPrice();
        } else if (testBuy.getPrice() == 0 || testBuy.getPrice() > testBought.getPrice()) {
                transactedPrice = testBought.getPrice();
        } else if (testBuy.getPrice() < testBought.getPrice()) {
                return;
        }

        tradeService.reconcileMarket(TestConstants.SYMBOL);

        assertEquals("FILLED", testSell.getStatus());
        assertEquals("PARTIALLY_FILLED", testBuy.getStatus());
    }

    @Test
    public void reconcileMarket_BetterLimitBuy_MatchLimitOrder(){
        // check partial filled
        // check average price
        // do for both buys and sells
    }

    @Test
    public void reconcileMarket_SamePrice_MatchEarlierOrder(){
        // check partial filled
        // check average price
        // do for both buys and sells

    }
}
