package cs203t10.ryver.market.trade;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;

import java.util.Date;

@RestController
public class TradeController {

    @Autowired
    TradeService tradeService;

    @GetMapping("/trades/{tradeId}")
    public TradeView getTrade(@PathVariable Integer tradeId) {
        return new TradeView(Action.BUY, "A17U", 1000, 0.0, 3.0, 2.9, 0, new Date(), 3, 2, Status.OPEN);
        // return tradeService.getTrade(tradeId);
    }

    @PostMapping("/trades")
    @PreAuthorize("principal.uid == #tradeView.getCustomerId() and hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeView addTrade(@Valid @RequestBody TradeView tradeView) {
        return null;
    }

}

