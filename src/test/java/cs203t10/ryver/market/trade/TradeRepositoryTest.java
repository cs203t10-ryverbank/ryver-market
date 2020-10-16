package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRepository;
import cs203t10.ryver.market.stock.StockException.NoSuchStockException;
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

    Trade tradeA1 = Trade.builder()
            .stock(a1).action(Action.BUY)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(firstDate)
            .status(Status.OPEN).price(1.18).build();
    // Same dates.
    Trade tradeB2_1 = Trade.builder()
            .stock(b2).action(Action.BUY)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(secondDate)
            .status(Status.OPEN).price(1.18).build();
    Trade tradeB2_2 = Trade.builder()
            .stock(b2).action(Action.BUY)
            .quantity(10000).filledQuantity(0)
            .customerId(2).accountId(2)
            .submittedDate(secondDate)
            .status(Status.OPEN).price(1.17).build();

    @BeforeEach
    public void saveInitialStocks() {
        stockRepo.saveAll(List.of(a1, b2, c3));
    }

    @AfterEach
    public void resetStocksAndTrades() {
        stockRepo.deleteAll();
        tradeRepo.deleteAll();
    }

    @Test
    public void saveWithSymbolTest() {
        // Set up trade to save.
        Trade tradeA1WithoutStock = tradeA1.toBuilder().stock(null).build();

        // Save the trade, which should populate the id and stock fields.
        Trade savedTrade = tradeRepo.saveWithSymbol(tradeA1WithoutStock, a1.getSymbol());

        // Remove the Id from the saved trade for comparison's sake.
        Trade savedTradeWithoutId = savedTrade.toBuilder().id(null).build();

        assertEquals(tradeA1, savedTradeWithoutId);
    }

    @Test
    public void saveWithSymbolTest_noSuchStock() {
        // Set up trade to save.
        Trade tradeA1WithoutStock = tradeA1.toBuilder().stock(null).build();

        // Save the trade with a non-existent stock symbol.
        assertThrows(NoSuchStockException.class, () -> {
            tradeRepo.saveWithSymbol(tradeA1WithoutStock, "Z26");
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
        Optional<Trade> latestNonExistentTrade = tradeRepo.findLatestBySymbol("Z26");

        assertEquals(Optional.empty(), latestNonExistentTrade);
    }

}

