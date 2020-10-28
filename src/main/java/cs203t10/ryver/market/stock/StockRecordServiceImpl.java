package cs203t10.ryver.market.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.exception.*;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public final class StockRecordServiceImpl implements StockRecordService {

    @Autowired
    private StockRecordRepository stockRecordRepo;

    @Override
    public List<StockRecord> getAllLatestStockRecords() {
        return stockRecordRepo.findAllLatestPerStock();
    }

    @Override
    public StockRecord getLatestStockRecordBySymbol(final String symbol) {
        return stockRecordRepo
                .findLatestBySymbol(symbol)
                .orElseThrow(() -> new NoSuchStockException(symbol));
    }

    @Override
    public StockRecord updateStockRecordRemoveFromMarket(
            final String symbol, final Double price, final Integer quantityToRemove) {
        StockRecord stockRecord = getLatestStockRecordBySymbol(symbol);
        stockRecord.setPrice(price);
        Integer initialQuantity = stockRecord.getTotalVolume();
        stockRecord.setTotalVolume(initialQuantity - quantityToRemove);

        return stockRecordRepo.save(stockRecord);
    }

    @Override
    public StockRecord updateStockRecordAddToMarket(final String symbol, final Integer quantityToAdd) {
        StockRecord stockRecord = getLatestStockRecordBySymbol(symbol);
        Integer initialQuantity = stockRecord.getTotalVolume();
        stockRecord.setTotalVolume(initialQuantity + quantityToAdd);

        return stockRecordRepo.save(stockRecord);
    }
}

