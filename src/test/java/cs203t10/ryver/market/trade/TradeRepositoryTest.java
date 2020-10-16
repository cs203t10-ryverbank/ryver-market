package cs203t10.ryver.market.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;

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

}

