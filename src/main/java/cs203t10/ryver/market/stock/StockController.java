package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.view.StockRecordView;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RolesAllowed("USER")
public class StockController {

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private TradeService tradeService;

    @GetMapping("/stocks")
    @Operation(summary = "Get all stocks")
    @ApiResponse(responseCode = "200", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = StockRecordView[].class)))
    public List<StockRecordView> getAllLatestStockRecords() {
        List<StockRecord> latestStockRecords = stockRecordService.getAllLatestStockRecords();

        return latestStockRecords.stream()
                .map(record -> {
                    String symbol = record.getStock().getSymbol();
                    Integer bidVolume = getBidVolume(symbol);
                    Integer askVolume = getAskVolume(symbol);

                    Double bid = getBidPrice(record, bidVolume, symbol);
                    Double ask = getAskPrice(record, askVolume, symbol);
                    stockRecordService.updateStockRecord(symbol, bid, ask);

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
    @Operation(summary = "Get a stock by symbol")
    @ApiResponse(responseCode = "200", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = StockRecordView.class)))
    public StockRecordView getLatestStockRecord(@PathVariable String symbol) {
        StockRecord latestStockRecord = stockRecordService.getLatestStockRecordBySymbol(symbol);

        Integer bidVolume = getBidVolume(symbol);
        Integer askVolume = getAskVolume(symbol);

        Double bid = getBidPrice(latestStockRecord, bidVolume, symbol);
        Double ask = getAskPrice(latestStockRecord, askVolume, symbol);

        stockRecordService.updateStockRecord(symbol, bid, ask);

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
        Trade bestLimitBuy = tradeService.getBestLimitBuyBySymbol(symbol);

        if (bidVolume == 0){
            return record.getLastBid();
        }

        if (bestLimitBuy != null ){
            return bestLimitBuy.getPrice();
        }

        return record.getLastBid();
    }

    private Double getAskPrice(StockRecord record, Integer askVolume, String symbol){
        Trade bestLimitSell = tradeService.getBestLimitSellBySymbol(symbol);
        if (askVolume == 0){
            return record.getLastAsk();
        }
        if (bestLimitSell != null ){
            return bestLimitSell.getPrice();
        }
        return record.getLastAsk();
    }

    private Integer getBidVolume(String symbol){
        Trade bestBuy = tradeService.getBestBuyForStockView(symbol);
        if (bestBuy == null){
            return 0;
        }
        return bestBuy.getQuantity() - bestBuy.getFilledQuantity();
    }

    private Integer getAskVolume(String symbol){
        Trade bestSell = tradeService.getBestSellForStockView(symbol);
        if (bestSell == null){
            return 0;
        }
        return bestSell.getQuantity() - bestSell.getFilledQuantity();
    }
}

