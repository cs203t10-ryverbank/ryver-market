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
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.exception.TradeNotAllowedException;
import cs203t10.ryver.market.trade.exception.TradeNotFoundException;
import cs203t10.ryver.market.trade.view.TradeViewCreatable;
import cs203t10.ryver.market.trade.view.TradeViewViewable;
import cs203t10.ryver.market.util.DateService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RolesAllowed("USER")
public class TradeController {

    @Autowired
    private TradeService tradeService;
    @Autowired
    private DateService dateService;

    @Autowired
    private PrincipalService principalService;

    @GetMapping("/trades")
    @Operation(summary = "Get all user trades")
    @ApiResponse(responseCode = "200", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TradeViewViewable[].class)))
    public List<TradeViewViewable> getAllUserTrades() {
        RyverPrincipal principal = principalService.getPrincipal();
        return tradeService.getAllUserOpenTrades(principal.uid).stream()
                .map(TradeViewViewable::fromTrade)
                .map(tradeView -> {
                    if (tradeView.getStatus() == Status.INVALID) {
                        tradeView.setStatus(Status.PARTIAL_FILLED);
                    }
                    return tradeView;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/trades/{tradeId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get a user's trades")
    @ApiResponse(responseCode = "200", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TradeViewViewable.class)))
    public TradeViewViewable getTrade(@PathVariable Integer tradeId) {
        RyverPrincipal principal = principalService.getPrincipal();
        Trade retrievedTrade = tradeService.getTrade(tradeId);
        TradeViewViewable retrievedTradeView =  TradeViewViewable.fromTrade(retrievedTrade);
        if (retrievedTradeView.getStatus() == Status.INVALID) {
            retrievedTradeView.setStatus(Status.PARTIAL_FILLED);
        }
        if (principal.uid.intValue() != retrievedTradeView.getCustomerId()) {
            throw new TradeNotAllowedException(tradeId, principal.uid.intValue());
        }

        return retrievedTradeView;
    }

    @PostMapping("/trades")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Add trade")
    @ApiResponse(responseCode = "201", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TradeViewViewable.class)))
    @ResponseStatus(HttpStatus.CREATED)
    public TradeViewViewable addTrade(@Valid @RequestBody TradeViewCreatable tradeView) {
        RyverPrincipal principal = principalService.getPrincipal();

        Integer requesterId = principal.uid.intValue();
        Integer traderId = tradeView.getCustomerId();
        if (!requesterId.equals(traderId)) {
            throw new TradeNotAllowedException(requesterId);
        }

        Trade savedTrade = tradeService.saveTrade(tradeView);
        // TradeView savedTradeView = TradeView.fromTrade(savedTrade);
        // if (savedTradeView.getStatus() == Status.INVALID){
        //     savedTradeView.setStatus(Status.PARTIAL_FILLED);
        // }
        // return savedTradeView;

        return TradeViewViewable.fromTrade(savedTrade);
    }

    @PutMapping("/trades/{tradeId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancel trade")
    @ApiResponse(responseCode = "200", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TradeViewViewable.class)))
    public TradeViewViewable cancelTrade(@PathVariable Integer tradeId) {
        RyverPrincipal principal = principalService.getPrincipal();
        Integer requesterId = principal.uid.intValue();
        Trade tradeToCancel = tradeService.getTrade(tradeId);
        if (tradeToCancel == null) {
            throw new TradeNotFoundException(tradeId);
        }
        if (!requesterId.equals(tradeToCancel.getCustomerId())) {
            throw new TradeNotAllowedException(tradeId, requesterId);
        }
        return TradeViewViewable.fromTrade(tradeService.cancelTrade(tradeToCancel));
    }

    @PostMapping("/reset")
    @RolesAllowed("MANAGER")
    @Hidden
    public void resetTrades() {
        tradeService.resetTrades();
    }

    @PostMapping("/market/{value}")
    @RolesAllowed("MANAGER")
    @Hidden
    public String setMarketOpen(@PathVariable String value) {
        switch (value) {
            case "open":
                dateService.setOpen(true);
                break;
            case "close":
                dateService.setOpen(false);
                break;
            case "default":
                dateService.setOpen(null);
                break;
            default:
                break;
        }
        return "Market is " + (dateService.isArtificial() ? "artificial" : "default") + ", currently " + dateService.isMarketOpen();
    }


}

