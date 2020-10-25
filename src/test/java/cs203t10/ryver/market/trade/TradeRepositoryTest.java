package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRepository;
import cs203t10.ryver.market.stock.exception.*;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;

@DataJpaTest
public class TradeRepositoryTest {

    @Autowired
    StockRepository stockRepo;

    @Autowired
    TradeRepository tradeRepo;

    @Autowired
    TestEntityManager entityManager;

    Date firstDate = new Date(1602321010000L);
    Date secondDate = new Date(1602324610000L);

    Stock a1 = new Stock("A1");
    Stock b2 = new Stock("B2");
    Stock c3 = new Stock("C3");
    Stock d4 = new Stock("D4");
    final String FAKE_STOCK_SYMBOL = "Z26";

    Trade tradeA1_1 = Trade.builder()
            .stock(a1).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(firstDate)
            .status(Status.OPEN).price(1.18).build();

    // Same dates.
    Trade tradeB2_1 = Trade.builder()
            .stock(b2).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.18).build();

    Trade tradeB2_2 = Trade.builder()
            .stock(b2).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(2).accountId(2).submittedDate(secondDate)
            .status(Status.OPEN).price(1.17).build();

    @BeforeEach
    public void saveInitialStocks() {
        stockRepo.saveAll(List.of(a1, b2, c3, d4));
    }

    @AfterEach
    public void resetStocksAndTrades() {
        stockRepo.deleteAll();
        tradeRepo.deleteAll();
    }

    @Test
    public void saveWithSymbolTest() {
        // Set up trade to save.
        Trade tradeA1WithoutStock = tradeA1_1.toBuilder().stock(null).build();

        // Save the trade, which should populate the id and stock fields.
        Trade savedTrade = tradeRepo.saveWithSymbol(tradeA1WithoutStock, a1.getSymbol());

        // Remove the Id from the saved trade for comparison's sake.
        Trade savedTradeWithoutId = savedTrade.toBuilder().id(null).build();

        assertEquals(tradeA1_1, savedTradeWithoutId);
    }

    @Test
    public void saveWithSymbolTest_noSuchStock() {
        // Set up trade to save.
        Trade tradeA1WithoutStock = tradeA1_1.toBuilder().stock(null).build();

        // Save the trade with a non-existent stock symbol.
        assertThrows(NoSuchStockException.class, () -> {
            tradeRepo.saveWithSymbol(tradeA1WithoutStock, FAKE_STOCK_SYMBOL);
        });
        entityManager.clear();
    }

    @Test
    public void findLatestBySymbolTest() {
        // Set up trades to retrieve.
        String symbol = tradeB2_1.getStock().getSymbol();
        tradeRepo.saveWithSymbol(tradeB2_1, symbol);

        // Get the latest trade from database.
        Trade latestB2Trade = tradeRepo.findLatestBySymbol(symbol).get();

        assertEquals(tradeB2_1, latestB2Trade);
    }

    @Test
    public void findLatestBySymbolTest_multipleLatest() {
        // Set up trades to retrieve.
        String symbol = tradeB2_1.getStock().getSymbol();
        tradeRepo.saveWithSymbol(tradeB2_1, symbol);
        tradeRepo.saveWithSymbol(tradeB2_2, symbol);

        // Get the latest trade from database.
        Trade latestB2Trade = tradeRepo.findLatestBySymbol(symbol).get();

        assertEquals(tradeB2_1, latestB2Trade);
    }

    @Test
    public void findLatestBySymbolTest_noSuchStock() {
        // Get the latest trade from database.
        Optional<Trade> latestNonExistentTrade
                = tradeRepo.findLatestBySymbol(FAKE_STOCK_SYMBOL);

        assertEquals(Optional.empty(), latestNonExistentTrade);
    }

    @Test
    public void findAllByCustomerIdTest() {
        // A1_1 and B2_1 are by customerId 1, B2_2 are by customerId 2.
        tradeRepo.saveAll(List.of(tradeA1_1, tradeB2_1, tradeB2_2));

        List<Trade> customer1Trades = tradeRepo.findAllByCustomerId(1L);

        assertEquals(List.of(tradeA1_1, tradeB2_1), customer1Trades);
    }

    @Test
    public void findAllByCustomerIdTest_noCustomerById() {
        // A1_1 and B2_1 are by customerId 1, B2_2 are by customerId 2.
        tradeRepo.saveAll(List.of(tradeA1_1, tradeB2_1, tradeB2_2));

        List<Trade> customer3Trades = tradeRepo.findAllByCustomerId(3L);

        assertEquals(List.of(), customer3Trades);
    }

    // The best market buy is the earlier one.
    Trade tradeC3_bestMarketBuy = Trade.builder()
            .stock(c3).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(firstDate)
            .status(Status.OPEN).price(0.0).build();

    Trade tradeC3_worstMarketBuy = Trade.builder()
            .stock(c3).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(2).accountId(2).submittedDate(secondDate)
            .status(Status.OPEN).price(0.0).build();

    @Test
    public void findBestMarketBuyTest() {
        String symbol = tradeC3_bestMarketBuy.getStock().getSymbol();
        tradeRepo.saveAll(List.of(tradeC3_bestMarketBuy, tradeC3_worstMarketBuy));

        Trade bestMarketBuyFromRepo = tradeRepo.findBestMarketBuyBySymbol(symbol).get();

        assertEquals(tradeC3_bestMarketBuy, bestMarketBuyFromRepo);
    }

    @Test
    public void findBestMarketBuyTest_noSuchStock() {
        Optional<Trade> bestMarketBuyFromRepo
                = tradeRepo.findBestMarketBuyBySymbol(FAKE_STOCK_SYMBOL);

        assertEquals(Optional.empty(), bestMarketBuyFromRepo);
    }

    // The best market sell is the earlier one.
    Trade tradeC3_bestMarketSell = Trade.builder()
            .stock(c3).action(Action.SELL).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(firstDate)
            .status(Status.OPEN).price(0.0).build();

    Trade tradeC3_worstMarketSell = Trade.builder()
            .stock(c3).action(Action.SELL).quantity(10000).filledQuantity(0)
            .customerId(2).accountId(2).submittedDate(secondDate)
            .status(Status.OPEN).price(0.0).build();

    @Test
    public void findBestMarketSellTest() {
        String symbol = tradeC3_bestMarketSell.getStock().getSymbol();
        tradeRepo.saveAll(List.of(tradeC3_bestMarketSell, tradeC3_worstMarketSell));

        Trade bestMarketSellFromRepo = tradeRepo.findBestMarketSellBySymbol(symbol).get();

        assertEquals(tradeC3_bestMarketSell, bestMarketSellFromRepo);
    }

    @Test
    public void findBestMarketSellTest_noSuchStock() {
        Optional<Trade> bestMarketSellFromRepo
                = tradeRepo.findBestMarketSellBySymbol(FAKE_STOCK_SYMBOL);

        assertEquals(Optional.empty(), bestMarketSellFromRepo);
    }

    // The better limit buy offers more money per stock.
    Trade tradeC3_bestLimitBuy_earliest = Trade.builder()
            .stock(c3).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(firstDate)
            .status(Status.OPEN).price(3.0).build();

    Trade tradeC3_bestLimitBuy = Trade.builder()
            .stock(c3).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(3.0).build();

    Trade tradeC3_worstLimitBuy = Trade.builder()
            .stock(c3).action(Action.BUY).quantity(10000).filledQuantity(0)
            .customerId(2).accountId(2).submittedDate(firstDate)
            .status(Status.OPEN).price(2.0).build();

    @Test
    public void findBestLimitBuyTest() {
        String symbol = tradeC3_bestLimitBuy.getStock().getSymbol();
        tradeRepo.saveAll(List.of(tradeC3_bestLimitBuy, tradeC3_worstLimitBuy));

        Trade bestLimitBuyFromRepo = tradeRepo.findBestLimitBuyBySymbol(symbol).get();

        assertEquals(tradeC3_bestLimitBuy, bestLimitBuyFromRepo);
    }

    @Test
    public void findBestLimitBuyTest_multipleBestPrice() {
        String symbol = tradeC3_bestLimitBuy.getStock().getSymbol();
        tradeRepo.saveAll(List.of(
                tradeC3_bestLimitBuy,
                tradeC3_bestLimitBuy_earliest,
                tradeC3_worstLimitBuy
        ));

        Trade bestLimitBuyFromRepo = tradeRepo.findBestLimitBuyBySymbol(symbol).get();

        assertEquals(tradeC3_bestLimitBuy_earliest, bestLimitBuyFromRepo);
    }

    @Test
    public void findBestLimitBuyTest_noSuchStock() {
        Optional<Trade> bestLimitBuyFromRepo
                = tradeRepo.findBestLimitBuyBySymbol(FAKE_STOCK_SYMBOL);

        assertEquals(Optional.empty(), bestLimitBuyFromRepo);
    }

    // The better limit sell costs less per stock.
    Trade tradeC3_bestLimitSell_earliest = Trade.builder()
            .stock(c3).action(Action.SELL).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(firstDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeC3_bestLimitSell = Trade.builder()
            .stock(c3).action(Action.SELL).quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeC3_worstLimitSell = Trade.builder()
            .stock(c3).action(Action.SELL).quantity(10000).filledQuantity(0)
            .customerId(2).accountId(2).submittedDate(firstDate)
            .status(Status.OPEN).price(2.0).build();

    @Test
    public void findBestLimitSellTest() {
        String symbol = tradeC3_bestLimitSell.getStock().getSymbol();
        tradeRepo.saveAll(List.of(tradeC3_bestLimitSell, tradeC3_worstLimitSell));

        Trade bestLimitSellFromRepo = tradeRepo.findBestLimitSellBySymbol(symbol).get();

        assertEquals(tradeC3_bestLimitSell, bestLimitSellFromRepo);
    }

    @Test
    public void findBestLimitSellTest_multipleBestPrice() {
        String symbol = tradeC3_bestLimitSell.getStock().getSymbol();
        tradeRepo.saveAll(List.of(
                tradeC3_bestLimitSell,
                tradeC3_bestLimitSell_earliest,
                tradeC3_worstLimitSell
        ));

        Trade bestLimitSellFromRepo = tradeRepo.findBestLimitSellBySymbol(symbol).get();

        assertEquals(tradeC3_bestLimitSell_earliest, bestLimitSellFromRepo);
    }

    @Test
    public void findBestLimitSellTest_noSuchStock() {
        Optional<Trade> bestLimitSellFromRepo
                = tradeRepo.findBestLimitSellBySymbol(FAKE_STOCK_SYMBOL);

        assertEquals(Optional.empty(), bestLimitSellFromRepo);
    }

    Trade tradeA1_1_Buy = Trade.builder()
            .stock(a1).action(Action.BUY).quantity(1000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeD4_1_Buy = Trade.builder()
            .stock(d4).action(Action.BUY).quantity(2000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeD4_2_Buy = Trade.builder()
            .stock(d4).action(Action.BUY).quantity(4000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeA1_1_Sell = Trade.builder()
            .stock(a1).action(Action.SELL).quantity(8000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeD4_1_Sell = Trade.builder()
            .stock(d4).action(Action.SELL).quantity(16000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();

    Trade tradeD4_2_Sell = Trade.builder()
            .stock(d4).action(Action.SELL).quantity(32000).filledQuantity(0)
            .customerId(1).accountId(1).submittedDate(secondDate)
            .status(Status.OPEN).price(1.5).build();


    @Test
    public void getBuyQuantityBySymbolTest() {
        tradeRepo.saveAll(List.of(
                tradeA1_1_Buy,
                tradeA1_1_Sell,
                tradeD4_1_Buy,
                tradeD4_2_Buy,
                tradeD4_1_Sell,
                tradeD4_2_Sell
        ));

        String symbol = tradeD4_1_Buy.getStock().getSymbol();
        Long quantityFromRepo = tradeRepo.getBuyQuantityBySymbol(symbol);

        Long expected = Stream.of(tradeD4_1_Buy, tradeD4_2_Buy)
            .map(Trade::getQuantity)
            .mapToLong(i -> (long) i)
            .sum();

        assertEquals(expected, quantityFromRepo);
    }

    @Test
    public void getSellQuantityBySymbolTest() {
        tradeRepo.saveAll(List.of(
                tradeA1_1_Buy,
                tradeA1_1_Sell,
                tradeD4_1_Buy,
                tradeD4_2_Buy,
                tradeD4_1_Sell,
                tradeD4_2_Sell
        ));

        String symbol = tradeD4_1_Buy.getStock().getSymbol();
        Long quantityFromRepo = tradeRepo.getSellQuantityBySymbol(symbol);

        Long expected = Stream.of(tradeD4_1_Sell, tradeD4_2_Sell)
            .map(Trade::getQuantity)
            .mapToLong(i -> (long) i)
            .sum();

        assertEquals(expected, quantityFromRepo);
    }

}

