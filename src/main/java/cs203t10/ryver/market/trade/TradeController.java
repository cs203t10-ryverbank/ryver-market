package cs203t10.ryver.market.trade;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeService;

@RestController
public class TradeController {

    @Autowired
    TradeService tradeService;

    @GetMapping("/trades/{tradeid}")
    public Trade getTrade(@PathVariable Integer tradeId) {
        return tradeService.getTrade(tradeId);
    }

    @PostMapping("/trades")
    @RolesAllowed("USER")
    @ResponseStatus(HttpStatus.CREATED)
    public Trade addTrade(@Valid @RequestBody Trade trade){
        Trade savedTrade= tradeService.saveTrade(trade);
        return savedTrade;
    }

}

