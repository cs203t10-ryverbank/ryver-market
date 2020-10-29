package cs203t10.ryver.market.trade;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cs203t10.ryver.market.security.PrincipalService;
import cs203t10.ryver.market.security.RyverPrincipal;
import cs203t10.ryver.market.trade.exception.TradeNotAllowedException;
import cs203t10.ryver.market.trade.exception.TradeNotFoundException;
import cs203t10.ryver.market.trade.view.TradeView;

import io.swagger.annotations.ApiOperation;

@RestController
@RolesAllowed("USER")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private PrincipalService principalService;

    @GetMapping("/trades")
    @ApiOperation(value = "Get all user trades")
    public List<TradeView> getAllUserTrades() {
        RyverPrincipal principal = principalService.getPrincipal();
        return tradeService.getAllUserOpenTrades(principal.uid).stream()
                .map(TradeView::fromTrade)
                .collect(Collectors.toList());
    }

    @GetMapping("/trades/{tradeId}")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Get a user's trades")
    public TradeView getTrade(@PathVariable Integer tradeId) {
        RyverPrincipal principal = principalService.getPrincipal();
        Trade retrievedTrade = tradeService.getTrade(tradeId);
        System.out.println("test: " + retrievedTrade == null);
        TradeView retrievedTradeView =  TradeView.fromTrade(retrievedTrade);

        if (principal.uid.intValue() != retrievedTradeView.getCustomerId()) {
            throw new TradeNotAllowedException(tradeId, principal.uid.intValue());
        }

        return retrievedTradeView;
    }

    @PostMapping("/trades")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Add trade")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeView addTrade(@Valid @RequestBody TradeView tradeView) {
        RyverPrincipal principal = principalService.getPrincipal();

        if (principal.uid.intValue() != tradeView.getCustomerId()) {
            throw new TradeNotAllowedException(tradeView.getCustomerId());
        }

        Trade savedTrade = tradeService.saveTrade(tradeView);
        return TradeView.fromTrade(savedTrade);
    }

    @PutMapping("/trades/{tradeId}")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Cancel trade")
    public TradeView cancelTrade(@PathVariable Integer tradeId) {
        RyverPrincipal principal = principalService.getPrincipal();
        Trade retrievedTrade = tradeService.getTrade(tradeId);
        if (retrievedTrade == null){
            throw new TradeNotFoundException(tradeId);
        }
        TradeView retrievedTradeView =  TradeView.fromTrade(retrievedTrade);
        if (principal.uid.intValue() != retrievedTradeView.getCustomerId()) {
            throw new TradeNotAllowedException(tradeId, principal.uid.intValue());
        }
        return TradeView.fromTrade(tradeService.cancelTrade(tradeId));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reset")
    @RolesAllowed("MANAGER")
    @ApiOperation(value = "Reset Market Trades")
    public void resetTrades() {
        tradeService.resetTrades();
    }

}

