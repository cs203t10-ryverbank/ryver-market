package cs203t10.ryver.market.portfolio;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;
import cs203t10.ryver.market.security.RyverPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/portfolio")
    @RolesAllowed("USER")
    public PortfolioInfoViewableByCustomer viewPortfolio(@AuthenticationPrincipal RyverPrincipal principal) {
        return portfolioService.viewPortfolio(Math.toIntExact(principal.uid));
    }

}

