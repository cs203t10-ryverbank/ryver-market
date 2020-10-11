package cs203t10.ryver.market.trade;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import cs203t10.ryver.market.security.RyverPrincipal;

import static cs203t10.ryver.market.trade.TradeException.CustomerNoAccessException;

@RestController
public class TradeController {

    @Autowired
    TradeService tradeService;

    @GetMapping("/trades/{tradeId}")
    public Trade getTrade(@PathVariable Integer tradeId) {
        return tradeService.getTrade(tradeId);
    }

    @PostMapping("/trades")
    @RolesAllowed("USER")
    @ResponseStatus(HttpStatus.CREATED)
    public Trade addTrade(@Valid @RequestBody Trade trade,
            @AuthenticationPrincipal RyverPrincipal principal) {
        if (Math.toIntExact(principal.uid) != trade.getCustomerId()) {
            throw new CustomerNoAccessException(trade.getCustomerId());
        }
        Trade savedTrade = null;
        savedTrade = tradeService.saveTrade(trade);
        return savedTrade;
    }

}

