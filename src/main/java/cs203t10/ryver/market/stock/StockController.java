package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.view.StockRecordView;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeService;

@RestController
public class StockController {

    @Autowired
    StockRecordService stockRecordService;

    @Autowired
    TradeService tradeService;

    @GetMapping("/stocks")
    public List<StockRecordView> getAllLatestStockRecords() {
        List<StockRecord> latestStockRecords = stockRecordService.getAllLatestStockRecords();
        return latestStockRecords.stream()
                .map(record -> {
                    String symbol = record.getStock().getSymbol();
                    return StockRecordView.fromRecordAskBid(record);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/stocks/{symbol}")
    public StockRecordView getLatestStockRecord(@PathVariable String symbol) {
        StockRecord latestStockRecord = stockRecordService.getLatestStockRecordBySymbol(symbol);
        Trade bestMarketBuy = tradeService.getBestMarketBuyBySymbol(symbol);
        Trade bestLimitBuy = tradeService.getBestLimitBuyBySymbol(symbol);
        Trade bestMarketSell = tradeService.getBestMarketSellBySymbol(symbol);
        Trade bestLimitSell = tradeService.getBestLimitSellBySymbol(symbol);
        return StockRecordView.fromRecordAskBid(
                latestStockRecord,
                getBestBuy(bestMarketBuy, bestLimitBuy),
                getBestSell(bestMarketSell, bestLimitSell)
        );
    }

    private static Trade getBestBuy(Trade buy, Trade otherBuy) {
        if (otherBuy == null && buy == null) return null;
        if (buy == null) return otherBuy;
        if (otherBuy == null) return buy;
        // The buy with a higher price is better, as it gives the
        // matcher (seller) more per stock traded.
        if (buy.getPrice() > otherBuy.getPrice()) {
            return buy;
        } else if (buy.getPrice() < otherBuy.getPrice()) {
            return otherBuy;
        }
        // If price is the same, then the earlier buy is returned.
        if (buy.getSubmittedDate().before(otherBuy.getSubmittedDate())) {
            return buy;
        }
        return otherBuy;
    }

    private static Trade getBestSell(Trade sell, Trade otherSell) {
        if (otherSell == null && sell == null) return null;
        if (sell == null) return otherSell;
        if (otherSell == null) return sell;
        // The sell with a lower price is better, as it lets the
        // matcher (buyer) get more stocks for a lower price.
        if (sell.getPrice() < otherSell.getPrice()) {
            return sell;
        } else if (sell.getPrice() > otherSell.getPrice()) {
            return otherSell;
        }
        // If price is the same, then the earlier sell is returned.
        if (sell.getSubmittedDate().before(otherSell.getSubmittedDate())) {
            return sell;
        }
        return otherSell;
    }

}

