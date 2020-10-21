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
import cs203t10.ryver.market.trade.TradeException.TradeNotFoundException;

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
    @PostAuthorize("principal.uid == returnObject.getCustomerId()")
    public TradeView getTrade(@PathVariable Integer tradeId) {
        return TradeView.fromTrade(tradeService.getTrade(tradeId));
    }

    @PostMapping("/trades")
    @PreAuthorize("principal.uid == #tradeView.getCustomerId()")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeView addTrade(@Valid @RequestBody TradeView tradeView) {
        Trade savedTrade = tradeService.saveTrade(tradeView);
        return TradeView.fromTrade(savedTrade);
    }

    @PutMapping("/trades/{tradeId}")
    @PreAuthorize("hasRole('USER')")
    public TradeView cancelTrade(@PathVariable Integer tradeId) {
        return TradeView.fromTrade(tradeService.cancelTrade(tradeId));
    }

}

