package cs203t10.ryver.market.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.view.TradeViewCreatable;
import cs203t10.ryver.market.stock.exception.*;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class StockRecordServiceImpl implements StockRecordService {

    @Autowired
    private StockRecordRepository stockRecordRepo;



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
    public StockRecord updateStockRecordRemoveFromMarket(
            String symbol, Double price, Integer quantityToRemove) {
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

    @Override
    public StockRecord updateStockRecord(String symbol, Double lastBid, Double lastAsk) {
        StockRecord stockRecord = getLatestStockRecordBySymbol(symbol);
        stockRecord.setLastBid(lastBid);
        stockRecord.setLastAsk(lastAsk);

        return stockRecordRepo.save(stockRecord);
    }

    @Override
    public StockRecord updateStockRecord(String symbol, Trade bestBuy, Trade bestSell) {
        StockRecord stockRecord = getLatestStockRecordBySymbol(symbol);
        if (bestSell != null && bestSell.getPrice() != 0.0){
            stockRecord.setLastAsk(bestSell.getPrice());
        }
        if (bestBuy != null && bestBuy.getPrice() != 0.0){
            stockRecord.setLastBid(bestBuy.getPrice());
        }
        return stockRecordRepo.save(stockRecord);
    }

    @Override
    public void reset(){
        stockRecordRepo.deleteAll();
    }

    @Override
    public void updateLastBidOnStockRecord(TradeViewCreatable tradeView){
        // Get latest stock
        StockRecord latestStock = getLatestStockRecordBySymbol(tradeView.getSymbol());
        boolean isMarketBuy = tradeView.getBid() == 0;

        Double bid = isMarketBuy
            ? latestStock.getLastAsk() : tradeView.getBid();

        // Update lastBuy on stock records if it is not market buy
        if (bid > latestStock.getLastBid() && !isMarketBuy) {
            latestStock.setLastBid(bid);
            updateStockRecord(tradeView.getSymbol(),
                            latestStock.getLastBid(),
                            latestStock.getLastAsk());
        }
    }

    @Override
    public void updateLastAskOnStockRecord(TradeViewCreatable tradeView){
        // Get latest stock
        StockRecord latestStock = getLatestStockRecordBySymbol(tradeView.getSymbol());

        boolean isMarketSell = tradeView.getAsk() == 0;

        // If it is a market sell, set to last bid.
        Double ask = isMarketSell
            ? latestStock.getLastBid() : tradeView.getAsk();

        // Update lastAsk on stock records if it is not a market sell
        if ( ask < latestStock.getLastAsk() && !isMarketSell){
            latestStock.setLastAsk(ask);
            updateStockRecord(tradeView.getSymbol(),
                            latestStock.getLastBid(),
                            latestStock.getLastAsk());
        }
    }

}

