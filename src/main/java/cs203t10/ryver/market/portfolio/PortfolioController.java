package cs203t10.ryver.market.portfolio;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

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
    @ApiOperation(value = "View Portfolio")
    public PortfolioInfoViewableByCustomer viewPortfolio(@AuthenticationPrincipal RyverPrincipal principal) {
        return portfolioService.viewPortfolio(Math.toIntExact(principal.uid));
    }

    @PutMapping("/portfolio/{customerId}/addToInitialCapital")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Add to Initial Capital")
    public void addToInitialCapital(@AuthenticationPrincipal RyverPrincipal principal,
        @Valid @RequestParam(value = "amount") Double amount) {
        Integer customerId = principal.uid.intValue();
        portfolioService.addToInitialCapital(customerId, amount);
    }

    @PutMapping("/portfolio/{customerId}/deductFromInitialCapital")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Deduct from Initial Capital")
    public void deductFromInitialCapital(@AuthenticationPrincipal RyverPrincipal principal,
        @Valid @RequestParam(value = "amount") Double amount) {
        Integer customerId = principal.uid.intValue();
        portfolioService.deductFromInitialCapital(customerId, amount);
    }
}

