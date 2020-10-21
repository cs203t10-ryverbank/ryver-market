package cs203t10.ryver.market.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cs203t10.ryver.market.stock.StockException.NoSuchStockException;

@ExtendWith(MockitoExtension.class)
public class StockRecordServiceTest {

    @Mock
    StockRecordRepository stockRecordRepo;

    @InjectMocks
    StockRecordServiceImpl stockRecordService;

    Date firstDate = new Date(1602321010000L);
    Date secondDate = new Date(1602324610000L);

    final StockRecord a1_1 = StockRecord.builder()
            .stock(new Stock("A1")).submittedDate(firstDate)
            .price(1.0).totalVolume(1000).build();
    final String EXISTS = "TEST";
    final String NOT_EXISTS = "NULL";

    @Test
    public void getAllLatestStockRecordsTest() {
        when(stockRecordRepo.findAllLatestPerStock())
            .thenReturn(new ArrayList<StockRecord>());

        var result = stockRecordService.getAllLatestStockRecords();

        assertEquals(new ArrayList<StockRecord>(), result);
        verify(stockRecordRepo).findAllLatestPerStock();
    }

    @Test
    public void getLatestStockRecordBySymbolTest_stockExists() {
        when(stockRecordRepo.findLatestBySymbol(EXISTS))
            .thenReturn(Optional.of(a1_1));

        var result = stockRecordService
            .getLatestStockRecordBySymbol(EXISTS);

        assertEquals(a1_1, result);
        verify(stockRecordRepo).findLatestBySymbol(EXISTS);
    }

    @Test
    public void getLatestStockRecordBySymbolTest_stockNonExist_throws() {
        when(stockRecordRepo.findLatestBySymbol(NOT_EXISTS))
            .thenReturn(Optional.empty());

        assertThrows(NoSuchStockException.class, () -> {
            stockRecordService.getLatestStockRecordBySymbol(NOT_EXISTS);
        });
        verify(stockRecordRepo).findLatestBySymbol(NOT_EXISTS);
    }

}

