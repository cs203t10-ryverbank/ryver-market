package cs203t10.ryver.market.trade;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import cs203t10.ryver.market.security.RyverPrincipal;
import cs203t10.ryver.market.trade.view.TradeView;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RolesAllowed("USER")
public class TradeController {

    @Autowired
    TradeService tradeService;

    @GetMapping("/trades")
    public List<TradeView> getAllUserTrades(@AuthenticationPrincipal RyverPrincipal principal) {
        System.out.println("\n\n\n\n HELLO!!! " + principal.username);
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

}

