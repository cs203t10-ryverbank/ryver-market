package cs203t10.ryver.market.stock;

import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.stock.view.StockView;

@RestController
public class StockController {

    @GetMapping("/stocks")
    public StockView getDefaultStock() {
        return StockView.builder()
                .symbol("A17U")
                .lastPrice(3.28)
                .bidVolume(20000)
                .bid(3.26)
                .askVolume(20000)
                .ask(3.29).build();
    }

}

