package cs203t10.ryver.market.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.service.StockRecordService;

@RestController
public class StockController {

    @Autowired
    StockRecordService stockRecordService;

    @GetMapping("/stocks")
    public List<StockRecord> getAllStockRecords() {
        return stockRecordService.getAllStockRecords();
    }

}

