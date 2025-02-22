package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
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
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeViewCreatable;
import cs203t10.ryver.market.trade.view.TradeViewViewable;
import cs203t10.ryver.market.util.DateService;
import cs203t10.ryver.market.exception.TradeNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    TradeRepository tradeRepo;

    @Mock
    FundTransferService fundTransferService;

    @Mock
    StockRecordService stockRecordService;

    @Mock
    DateService dateService;

    @InjectMocks
    TradeServiceImpl tradeService;

    TradeViewCreatable marketMakerBuy = TradeViewCreatable.builder()
        .action(Action.BUY).symbol(TestConstants.STOCK.getSymbol())
        .quantity(TestConstants.BUY_QUANTITY).filledQuantity(0)
        .customerId(0).accountId(0)
        .submittedDate(TestConstants.FIRST_DATE)
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

        List<Trade> trades = List.of(trade1, trade2, trade3, trade4, trade5);
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

        List<Trade> trades = List.of(trade1, trade2, trade3, trade4, trade5);
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
        TradeViewCreatable testBuy = TradeViewCreatable.builder()
            .action(Action.BUY).symbol(TestConstants.SYMBOL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(50)
            .submittedDate(TestConstants.FIRST_DATE)
            .bid(2.0).avgPrice(0.0).build();

        StockRecord testStockRecord = StockRecord.builder()
                .stock(TestConstants.STOCK).submittedDate(TestConstants.FIRST_DATE)
                .price(1.0).totalVolume(1000).build();


        when(dateService.getCurrentDate())
                .thenReturn(TestConstants.FIRST_DATE);
        when(stockRecordService.getLatestStockRecordBySymbol(TestConstants.SYMBOL))
                .thenReturn(testStockRecord);

        Mockito.doThrow(new AccountNotAllowedException(1, 50))
                .when(fundTransferService)
                .deductAvailableBalance(any(Integer.class), any(Integer.class), any(Double.class));

        assertThrows(AccountNotAllowedException.class, () -> {
            tradeService.saveTrade(testBuy);
        });
        verifyNoInteractions(tradeRepo);
        verify(dateService).getCurrentDate();
        verify(stockRecordService).getLatestStockRecordBySymbol(TestConstants.SYMBOL);

    }

    @Test
    public void saveTrade_InsufficientBalance_noRegister() {
        TradeViewCreatable testBuy = TradeViewCreatable.builder()
            .action(Action.BUY).symbol(TestConstants.SYMBOL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(TestConstants.FIRST_DATE)
            .bid(2.0).avgPrice(0.0).build();

        Mockito.doThrow(new InsufficientBalanceException(1, 20000.0))
                .when(fundTransferService)
                .deductAvailableBalance(any(Integer.class), any(Integer.class), any(Double.class));

        assertThrows(InsufficientBalanceException.class, () -> {
            tradeService.saveTrade(testBuy);
        });
        verifyNoInteractions(tradeRepo);
    }
}

