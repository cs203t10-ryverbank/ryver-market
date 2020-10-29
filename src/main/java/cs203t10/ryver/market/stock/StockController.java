package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.view.StockRecordView;
import cs203t10.ryver.market.trade.TradeService;

@RestController
@RolesAllowed("USER")
public class StockController {

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private TradeService tradeService;

    @GetMapping("/stocks")
    public List<StockRecordView> getAllLatestStockRecords() {
        List<StockRecord> latestStockRecords = stockRecordService.getAllLatestStockRecords();

        return latestStockRecords.stream()
                .map(record -> {
                    String symbol = record.getStock().getSymbol();
                    Integer bidVolume = tradeService.getTotalAskVolume(symbol);
                    Integer askVolume = tradeService.getTotalAskVolume(symbol);
                    return StockRecordView.fromRecordAskBid(
                            record,
                            tradeService.getBestBuy(symbol),
                            tradeService.getBestSell(symbol),
                            bidVolume,
                            askVolume
                    );
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/stocks/{symbol}")
    public StockRecordView getLatestStockRecord(@PathVariable String symbol) {
        StockRecord latestStockRecord = stockRecordService.getLatestStockRecordBySymbol(symbol);
        Integer bidVolume = tradeService.getTotalBidVolume(symbol);
        Integer askVolume = tradeService.getTotalAskVolume(symbol);
        return StockRecordView.fromRecordAskBid(
                latestStockRecord,
                tradeService.getBestBuy(symbol),
                tradeService.getBestSell(symbol),
                bidVolume,
                askVolume

        );
    }

}

