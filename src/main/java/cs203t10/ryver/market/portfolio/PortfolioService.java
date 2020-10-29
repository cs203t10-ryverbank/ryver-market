package cs203t10.ryver.market.portfolio;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;

public interface PortfolioService {
    Portfolio findByCustomerId(Integer customerId);
    Portfolio findByCustomerIdElseCreate(Integer customerId);
    Portfolio savePortfolio(PortfolioInitial portfolioInitial);
    PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId);
    Integer getQuantityOfAsset(Integer customerId, String code);
    Portfolio addToInitialCapital(Integer customerId, Double amount);
    Portfolio deductFromInitialCapital(Integer customerId, Double amount);
    Portfolio processBuyTrade(Trade trade);
    Portfolio processSellTrade(Trade trade);
    void resetPortfolios();
}

