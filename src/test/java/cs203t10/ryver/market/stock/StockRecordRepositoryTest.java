package cs203t10.ryver.market.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class StockRecordRepositoryTest {

    @Autowired
    StockRepository stockRepo;

    @Autowired
    StockRecordRepository stockRecordRepo;

    @Autowired
    TestEntityManager entityManager;

    Date firstDate = new Date(1602321010000L);
    Date secondDate = new Date(1602324610000L);

    StockRecord a1_1 = StockRecord.builder()
            .stock(new Stock("A1")).submittedDate(firstDate)
            .price(1.0).totalVolume(1000).build();
    // Same price, different dates
    StockRecord b2_1 = StockRecord.builder()
            .stock(new Stock("B2")).submittedDate(firstDate)
            .price(2.0).totalVolume(2000).build();
    StockRecord b2_2 = StockRecord.builder()
            .stock(new Stock("B2")).submittedDate(secondDate)
            .price(2.0).totalVolume(1500).build();
    // Same dates, different price
    StockRecord c3_1 = StockRecord.builder()
            .stock(new Stock("C3")).submittedDate(firstDate)
            .price(3.0).totalVolume(3000).build();
    StockRecord c3_2 = StockRecord.builder()
            .stock(new Stock("C3")).submittedDate(firstDate)
            .price(2.5).totalVolume(2500).build();


    @BeforeEach
    public void saveInitialRecords() {
        stockRepo.saveAll(List.of(
                new Stock("A1"),
                new Stock("B2"),
                new Stock("C3")
        ));
        stockRecordRepo.saveAll(List.of(a1_1, b2_1, b2_2, c3_1, c3_2));
    }

    @AfterEach
    public void resetRecords() {
        stockRepo.deleteAll();
        stockRecordRepo.deleteAll();
    }

    @Test
    public void findAllBySymbolTest() {
        List<StockRecord> b2Result = stockRecordRepo.findAllBySymbol("B2");
        assertEquals(List.of(b2_1, b2_2), b2Result);
    }

    @Test
    public void findAllBySymbolNoSuchStockTest() {
        List<StockRecord> emptyResult = stockRecordRepo.findAllBySymbol("Z26");
        assertEquals(List.of(), emptyResult);
    }

    @Test
    public void findAllLatestPerStockTest() {
        List<StockRecord> latestResult = stockRecordRepo.findAllLatestPerStock();
        assertEquals(List.of(a1_1, b2_2, c3_1, c3_2), latestResult);
    }

    @Test
    public void findLatestBySymbolTest() {
        Optional<StockRecord> latest = stockRecordRepo.findLatestBySymbol("B2");
        assertEquals(Optional.of(b2_2), latest);
    }

    @Test
    public void findLatestBySymbolNoSuchStockTest() {
        Optional<StockRecord> latest = stockRecordRepo.findLatestBySymbol("Z26");
        assertEquals(Optional.empty(), latest);
    }

}

