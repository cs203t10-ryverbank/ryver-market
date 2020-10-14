package cs203t10.ryver.market.portfolio;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cs203t10.ryver.market.security.RyverPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
public class PortfolioController {
    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/portfolio")
    @RolesAllowed("USER")
    public Portfolio getPortfolioByCustomerId(@AuthenticationPrincipal RyverPrincipal principal) {
        return portfolioService.findByCustomerId(Math.toIntExact(principal.uid));
    }

}
