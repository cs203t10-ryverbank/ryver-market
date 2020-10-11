package cs203t10.ryver.market.trade;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import cs203t10.ryver.market.security.RyverPrincipal;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeService;

import static cs203t10.ryver.market.trade.TradeException.*;

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
    public Trade addTrade(@Valid @RequestBody Trade trade, @AuthenticationPrincipal RyverPrincipal principal){
        if (Math.toIntExact(principal.uid) != trade.getCustomerId()) {
            throw new CustomerNoAccessException(trade.getCustomerId());
        }
        Trade savedTrade = null;
        savedTrade = tradeService.saveTrade(trade);
        return savedTrade;
    }

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public void wrongSymbolError(MethodArgumentNotValidException ex) {
    //     throw new StockSymbolInvalidException(ex.getMessage());
    // }
}

