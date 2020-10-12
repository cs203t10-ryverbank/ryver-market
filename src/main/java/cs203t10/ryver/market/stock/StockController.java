package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.view.StockRecordView;

@RestController
public class StockController {

    @Autowired
    StockRecordService stockRecordService;

    @GetMapping("/stocks")
    public List<StockRecordView> getAllLatestStockRecords() {
        List<StockRecord> latestStockRecords = stockRecordService.getAllLatestStockRecords();
        return latestStockRecords.stream()
                .map(StockRecordView::fromRecordAskBid)
                .collect(Collectors.toList());
    }

}

