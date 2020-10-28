package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.fund.exception.*;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.exception.*;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.exception.TradeNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    TradeRepository tradeRepo;

    @Mock
    FundTransferService fundTransferService;

    @InjectMocks
    TradeServiceImpl tradeService;

    Date firstDate = new Date(1603962000L);
    Date secondDate = new Date(1603904242L);

    final Integer EXISTS_ID = 1;
    final Integer NOT_EXISTS_ID = 2;

    final Integer CUSTOMER_ID = 1;
    final Integer ACCOUNT_ID = 50;
    final String SYMBOL = "A1";
    final Stock STOCK = new Stock(SYMBOL);

    final Integer BUY_QUANTITY = 10000;
    final Integer SELL_QUANTITY = 10000;
    final Integer PARTIAL_QUANTITY = 5000;
    final Double HIGH_PRICE = 3.0;
    final Double LOW_PRICE = 2.0;

    TradeView marketMakerBuy = TradeView.builder()
            .action(Action.BUY).symbol(STOCK.getSymbol())
            .quantity(BUY_QUANTITY).filledQuantity(0)
            .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
            .submittedDate(firstDate).status(Status.OPEN)
            .bid(LOW_PRICE).avgPrice(0.0).build();

    Trade marketBuy = Trade.builder()
            .stock(STOCK).action(Action.BUY)
            .quantity(BUY_QUANTITY).filledQuantity(0)
            .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
            .submittedDate(firstDate).status(Status.OPEN)
            .price(0.0).build();

    Trade marketSell = Trade.builder()
            .stock(STOCK).action(Action.SELL)
            .quantity(SELL_QUANTITY).filledQuantity(0)
            .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
            .submittedDate(firstDate).status(Status.OPEN)
            .price(0.0).build();

    @Test
    public void getBestBuy_BetterMarketBuy_ReturnMarketBuy(){
        Trade limitBuy = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(LOW_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(SELL_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();


        tradeRepo.saveAll(List.of(marketBuy, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketBuyBySymbol(SYMBOL)).thenReturn(Optional.of(marketBuy));
        when(tradeRepo.findBestLimitBuyBySymbol(SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestBuy(SYMBOL);
        Trade expected = marketBuy;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketBuyBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(SYMBOL);
    }

    @Test
    public void getBestBuy_BetterLimitBuy_ReturnLimitBuy(){
        Trade limitBuy = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(SELL_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();


        tradeRepo.saveAll(List.of(marketBuy, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketBuyBySymbol(SYMBOL)).thenReturn(Optional.of(marketBuy));
        when(tradeRepo.findBestLimitBuyBySymbol(SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestBuy(SYMBOL);
        Trade expected = limitBuy;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketBuyBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(SYMBOL);
    }

    @Test
    public void getBestSell_BetterMarketSell_ReturnMarketSell(){
        Trade limitBuy = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(LOW_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(SELL_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();


        tradeRepo.saveAll(List.of(marketSell, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketSellBySymbol(SYMBOL)).thenReturn(Optional.of(marketSell));
        when(tradeRepo.findBestLimitBuyBySymbol(SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestSell(SYMBOL);
        Trade expected = marketSell;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketSellBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(SYMBOL);
    }

    @Test
    public void getBestSell_BetterLimitSell_ReturnLimitSell(){
        Trade limitBuy = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();

        Trade limitSell = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(SELL_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(LOW_PRICE).build();


        tradeRepo.saveAll(List.of(marketSell, limitBuy, limitSell));

        when(tradeRepo.findBestLimitSellBySymbol(SYMBOL)).thenReturn(Optional.of(limitSell));
        when(tradeRepo.findBestMarketSellBySymbol(SYMBOL)).thenReturn(Optional.of(marketSell));
        when(tradeRepo.findBestLimitBuyBySymbol(SYMBOL)).thenReturn(Optional.of(limitBuy));

        Trade actual = tradeService.getBestSell(SYMBOL);
        Trade expected = limitSell;

        assertEquals(expected, actual);
        verify(tradeRepo).findBestMarketSellBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitBuyBySymbol(SYMBOL);
        verify(tradeRepo).findBestLimitSellBySymbol(SYMBOL);
    }

    @Test
    public void getTotalBidVolume_IgnoreExpiredCancelledFilled_ReturnValid(){
        Trade trade1 = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();

        Trade trade2 = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(PARTIAL_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.PARTIAL_FILLED)
                .price(LOW_PRICE).build();

        Trade trade3 = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(PARTIAL_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.EXPIRED)
                .price(LOW_PRICE).build();

        Trade trade4 = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(PARTIAL_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.CANCELLED)
                .price(LOW_PRICE).build();

        Trade trade5 = Trade.builder()
                .stock(STOCK).action(Action.BUY)
                .quantity(BUY_QUANTITY).filledQuantity(BUY_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.FILLED)
                .price(LOW_PRICE).build();

        List trades = List.of(trade1, trade2, trade3, trade4, trade5);
        tradeRepo.saveAll(trades);

        when(tradeRepo.findAllBuyTradesBySymbol(SYMBOL)).thenReturn(trades);

        Integer expected = 15000;
        Integer actual = tradeService.getTotalBidVolume(SYMBOL);
        assertEquals(expected, actual);
        verify(tradeRepo).findAllBuyTradesBySymbol(SYMBOL);
    }

    @Test
    public void getTotalAskVolume_IgnoreExpiredCancelledFilled_ReturnValid(){
        Trade trade1 = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(BUY_QUANTITY).filledQuantity(0)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.OPEN)
                .price(HIGH_PRICE).build();

        Trade trade2 = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(BUY_QUANTITY).filledQuantity(PARTIAL_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.PARTIAL_FILLED)
                .price(LOW_PRICE).build();

        Trade trade3 = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(BUY_QUANTITY).filledQuantity(PARTIAL_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.EXPIRED)
                .price(LOW_PRICE).build();

        Trade trade4 = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(BUY_QUANTITY).filledQuantity(PARTIAL_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.CANCELLED)
                .price(LOW_PRICE).build();

        Trade trade5 = Trade.builder()
                .stock(STOCK).action(Action.SELL)
                .quantity(BUY_QUANTITY).filledQuantity(BUY_QUANTITY)
                .customerId(CUSTOMER_ID).accountId(ACCOUNT_ID)
                .submittedDate(firstDate).status(Status.FILLED)
                .price(LOW_PRICE).build();

        List trades = List.of(trade1, trade2, trade3, trade4, trade5);
        tradeRepo.saveAll(trades);

        when(tradeRepo.findAllSellTradesBySymbol(SYMBOL)).thenReturn(trades);

        Integer expected = 15000;
        Integer actual = tradeService.getTotalAskVolume(SYMBOL);
        assertEquals(expected, actual);
        verify(tradeRepo).findAllSellTradesBySymbol(SYMBOL);
    }


    // public void getBestBuy_DifferentPrice_ReturnHigherBid(){

    // }

    // public void getBestSell_DifferentPrice_ReturnLowerBid(){

    // }

    // public void getBestSell_SamePrice_ReturnEarlierBid(){

    // }

    // Why does this make marketMakerBuy a nullpointer?
    // @Test
    // public void registerBuyTradeTest_marketMaker_noRegister() {
    //     tradeService.saveTrade(marketMakerBuy);

    //     // Ensure that the fund transfer service is not called.
    //     verifyNoInteractions(fundTransferService);
    //     verify(tradeRepo).saveWithSymbol(
    //         marketMakerBuy.toTrade(),
    //         marketMakerBuy.getSymbol());
    // }

    @Test
    public void getTradeTest_nonExistentTrade() {
        when(tradeRepo.findById(NOT_EXISTS_ID))
            .thenReturn(Optional.empty());

        assertThrows(TradeNotFoundException.class, () -> {
            tradeService.getTrade(NOT_EXISTS_ID);
        });
    }

    @Test
    public void saveTrade_AccountDoesNotBelongToCustomer_noRegister() {
        TradeView testBuy = TradeView.builder()
            .action(Action.BUY).symbol(SYMBOL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(50)
            .submittedDate(firstDate).status(Status.OPEN)
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
            .action(Action.BUY).symbol(SYMBOL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate).status(Status.OPEN)
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
    //         .submittedDate(firstDate).status(Status.OPEN)
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

}
