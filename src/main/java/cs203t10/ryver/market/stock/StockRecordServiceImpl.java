package cs203t10.ryver.market.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.*;

import static cs203t10.ryver.market.stock.StockException.NoSuchStockException;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class StockRecordServiceImpl implements StockRecordService {

    @Autowired
    TradeService tradeService;

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

    public StockRecord createStockRecord(Integer tradeId) {
        Trade trade = tradeService.getTrade(tradeId);

        StockRecord stockRecord = new StockRecord();
        stockRecord.setStock(trade.getStock());
        stockRecord.setPrice(trade.getPrice());
        stockRecord.setTotalVolume(trade.getQuantity());

        return stockRecordRepo.save(stockRecord);
    }
}

