package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.view.StockRecordView;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeService;

import io.swagger.annotations.ApiOperation;

@RestController
@RolesAllowed("USER")
public class StockController {

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private TradeService tradeService;

    @GetMapping("/stocks")
    @ApiOperation(value = "Get all stocks")
    public List<StockRecordView> getAllLatestStockRecords() {
        List<StockRecord> latestStockRecords = stockRecordService.getAllLatestStockRecords();

        return latestStockRecords.stream()
                .map(record -> {
                    String symbol = record.getStock().getSymbol();
                    Integer bidVolume = tradeService.getTotalAskVolume(symbol);
                    Integer askVolume = tradeService.getTotalAskVolume(symbol);

                    Double bid = getBidPrice(record, bidVolume, symbol);
                    Double ask = getAskPrice(record, askVolume, symbol);

                    return StockRecordView.fromRecordAskBid(
                            record,
                            bid,
                            ask,
                            bidVolume,
                            askVolume
                    );
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/stocks/{symbol}")
    @ApiOperation(value = "Get a stock by symbol")
    public StockRecordView getLatestStockRecord(@PathVariable String symbol) {
        StockRecord latestStockRecord = stockRecordService.getLatestStockRecordBySymbol(symbol);
        Integer bidVolume = tradeService.getTotalBidVolume(symbol);
        Integer askVolume = tradeService.getTotalAskVolume(symbol);
        Double bid = getBidPrice(latestStockRecord, bidVolume, symbol);
        Double ask = getAskPrice(latestStockRecord, askVolume, symbol);

        if (ask == 0.0) {
            ask = latestStockRecord.getLastAsk();
        }
        if (bid == 0.0){
            bid = latestStockRecord.getLastBid();
        }

        return StockRecordView.fromRecordAskBid(
                latestStockRecord,
                bid,
                ask,
                bidVolume,
                askVolume

        );
    }

    private Double getBidPrice(StockRecord record, Integer bidVolume, String symbol){
        // If bid or ask volume hits 0, set bid/ask to last bid/ask
        // Else, set bid/ask to best buy/sell
        Double bid = bidVolume == 0
            ? record.getLastBid() : tradeService.getBestBuy(symbol).getPrice() ;
        // If best buy or best sell is a market order,
        // set bid/ask to last bid/ last ask
        if (bid == 0.0){
            bid = record.getLastBid();
        }

        return bid;
    }

    private Double getAskPrice(StockRecord record, Integer askVolume, String symbol){
        Double ask = askVolume == 0
            ? record.getLastAsk() : tradeService.getBestSell(symbol).getPrice() ;

        if (ask == 0.0){
            ask = record.getLastAsk();
        }
        return ask;
    }
}

