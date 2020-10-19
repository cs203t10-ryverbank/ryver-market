package cs203t10.ryver.market.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static cs203t10.ryver.market.stock.StockException.NoSuchStockException;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class StockRecordServiceImpl implements StockRecordService {

    @Autowired
    StockRepository stockRepo;

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

    @Override
    public StockRecord updateStockRecordRemoveFromMarket(String symbol, Double price, Integer quantityToRemove) {
        StockRecord stockRecord = getLatestStockRecordBySymbol(symbol);
        stockRecord.setPrice(price);
        Integer initialQuantity = stockRecord.getTotalVolume();
        stockRecord.setTotalVolume(initialQuantity - quantityToRemove);

        return stockRecordRepo.save(stockRecord);
    }

    @Override
    public StockRecord updateStockRecordAddToMarket(String symbol, Integer quantityToAdd) {
        StockRecord stockRecord = getLatestStockRecordBySymbol(symbol);
        Integer initialQuantity = stockRecord.getTotalVolume();
        stockRecord.setTotalVolume(initialQuantity + quantityToAdd);

        return stockRecordRepo.save(stockRecord);
    }
}

