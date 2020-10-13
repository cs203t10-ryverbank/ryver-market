package cs203t10.ryver.market.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static cs203t10.ryver.market.portfolio.PortfolioException.*;

@Service
public class PortfolioServiceImpl implements PortfolioService{
    
    @Autowired
    private PortfolioRepository portfolios;

    public Portfolio findByCustomerId(Integer customerId) {
        return portfolios.findByCustomerId(customerId)
                .orElseThrow(() -> new PortfolioNotFoundException(customerId));
    }
}