package cs203t10.ryver.market.stock;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;

// @ExtendWith(MockitoExtension.class)
@DataJpaTest
public class StockRecordRepositoryTest {

    @Autowired
    StockRepository stockRepo;

    @Autowired
    StockRecordRepository stockRecordRepo;

    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    public void saveInitialRecords() {
        Date firstDate = new Date(1602321010000L);
        Date secondDate = new Date(1602324610000L);
        stockRepo.saveAll(List.of(
                new Stock("A1"),
                new Stock("B2"),
                new Stock("C3")
        ));
        stockRecordRepo.saveAll(List.of(
                StockRecord.builder()
                    .stock(new Stock("A1")).submittedDate(firstDate)
                    .price(1.0).totalVolume(1000).build(),
                // Same price, different dates
                StockRecord.builder()
                    .stock(new Stock("B2")).submittedDate(firstDate)
                    .price(2.0).totalVolume(2000).build(),
                StockRecord.builder()
                    .stock(new Stock("B2")).submittedDate(secondDate)
                    .price(2.0).totalVolume(1500).build(),
                // Same dates, different price
                StockRecord.builder()
                    .stock(new Stock("C3")).submittedDate(firstDate)
                    .price(3.0).totalVolume(3000).build(),
                StockRecord.builder()
                    .stock(new Stock("A1")).submittedDate(firstDate)
                    .price(2.5).totalVolume(2500).build()
        ));
    }

    @AfterEach
    public void resetRecords() {
        stockRecordRepo.deleteAll();
    }

    @Test
    public void findAllBySymbolTest() {

    }

}

