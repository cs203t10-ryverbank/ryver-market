package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
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

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    TradeRepository tradeRepo;

    @Mock
    FundTransferService fundTransferService;

    @InjectMocks
    TradeServiceImpl tradeService;

    Date firstDate = new Date(1602321010000L);
    Date secondDate = new Date(1602324610000L);

    Stock a1 = new Stock("A1");
    final Integer EXISTS_ID = 1;
    final Integer NOT_EXISTS_ID = 2;

    TradeView marketMakerBuy = TradeView.builder()
            .action(Action.BUY).symbol(a1.getSymbol())
            .quantity(10000).filledQuantity(0)
            .customerId(0).accountId(0)
            .submittedDate(firstDate).status(Status.OPEN)
            .bid(2.0).avgPrice(0.0).build();

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
            .action(Action.BUY).symbol(a1.getSymbol())
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
            .action(Action.BUY).symbol(a1.getSymbol())
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

    @Test
    public void saveTrade_ValidTrade_Register() {
        TradeView testBuy = TradeView.builder()
            .action(Action.BUY).symbol(a1.getSymbol())
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate).status(Status.OPEN)
            .bid(2.0).avgPrice(0.0).build();

        Mockito.doNothing()
            .when(fundTransferService)
            .deductAvailableBalance(any(Integer.class), any(Integer.class), any(Double.class));

        when(tradeRepo.saveWithSymbol(testBuy.toTrade(), testBuy.getSymbol()))
            .thenReturn(testBuy.toTrade());

        Trade testTrade = tradeService.saveTrade(testBuy);
        assertEquals(testBuy.toTrade(), testTrade);

        verify(fundTransferService).deductAvailableBalance(1, 1, 20000.0);

        verify(tradeRepo).saveWithSymbol(testBuy.toTrade(), testBuy.getSymbol());
    }

}
