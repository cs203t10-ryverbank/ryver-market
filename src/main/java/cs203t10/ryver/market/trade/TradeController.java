package cs203t10.ryver.market.trade;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import cs203t10.ryver.market.security.PrincipalService;
import cs203t10.ryver.market.security.RyverPrincipal;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.trade.exception.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RolesAllowed("USER")
public class TradeController {

    @Autowired
    TradeService tradeService;

    @Autowired
    PrincipalService principalService;

    @GetMapping("/trades")
    public List<TradeView> getAllUserTrades() {
        RyverPrincipal principal = principalService.getPrincipal();
        return tradeService.getAllUserOpenTrades(principal.uid).stream()
                .map(TradeView::fromTrade)
                .collect(Collectors.toList());
    }

    @GetMapping("/trades/{tradeId}")
    @PreAuthorize("hasRole('USER')")
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
    public TradeView cancelTrade(@PathVariable Integer tradeId) {
        return TradeView.fromTrade(tradeService.cancelTrade(tradeId));
    }

}

