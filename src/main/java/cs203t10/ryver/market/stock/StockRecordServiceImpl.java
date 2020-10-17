package cs203t10.ryver.market.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.exception.*;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class StockRecordServiceImpl implements StockRecordService {

    @Autowired
    StockRecordRepository stockRecordRepo;

    @Override
    public List<StockRecord> getAllLatestStockRecords() {
        return stockRecordRepo.findAllLatestPerStock();
    }

    @Override
    public StockRecord getLatestStockRecordBySymbol(String symbol) {
        return stockRecordRepo
                .findLatestBySymbol(symbol)
                .orElseThrow(() -> new NoSuchStockException(symbol));
    }

}

