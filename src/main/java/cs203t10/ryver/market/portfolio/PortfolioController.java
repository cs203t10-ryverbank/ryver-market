package cs203t10.ryver.market.portfolio;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
public class PortfolioController {
    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/portfolio")
    @RolesAllowed("USER")
    public Portfolio getPortfolioByCustomerId(@AuthenticationPrincipal Integer customerId) {
        return portfolioService.findByCustomerId(customerId);
    }

}