package cs203t10.ryver.market.stock;

import java.util.List;
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
                    return StockRecordView.fromRecordAskBid(
                            record,
                            tradeService.getBestBuy(symbol),
                            tradeService.getBestSell(symbol)
                    );
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/stocks/{symbol}")
    public StockRecordView getLatestStockRecord(@PathVariable String symbol) {
        StockRecord latestStockRecord = stockRecordService.getLatestStockRecordBySymbol(symbol);
        return StockRecordView.fromRecordAskBid(
                latestStockRecord,
                tradeService.getBestBuy(symbol),
                tradeService.getBestSell(symbol)
        );
    }

}

