package cs203t10.ryver.market.portfolio;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;
import cs203t10.ryver.market.security.RyverPrincipal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/portfolio")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "View Portfolio")
    @ApiResponse(responseCode = "200", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PortfolioInfoViewableByCustomer.class)))
    public PortfolioInfoViewableByCustomer viewPortfolio(@AuthenticationPrincipal RyverPrincipal principal) {
        return portfolioService.viewPortfolio(Math.toIntExact(principal.uid));
    }

    @PutMapping("/portfolio/{customerId}/addToInitialCapital")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @Hidden
    public void addToInitialCapital(@PathVariable Integer customerId,
        @Valid @RequestParam(value = "amount") Double amount) {
        portfolioService.addToInitialCapital(customerId, amount);
    }

    @PutMapping("/portfolio/{customerId}/deductFromInitialCapital")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Hidden
    public void deductFromInitialCapital(@AuthenticationPrincipal RyverPrincipal principal,
        @Valid @RequestParam(value = "amount") Double amount) {
        Integer customerId = principal.uid.intValue();
        portfolioService.deductFromInitialCapital(customerId, amount);
    }
}

