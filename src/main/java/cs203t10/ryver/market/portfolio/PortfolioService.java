package cs203t10.ryver.market.portfolio;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;

public interface PortfolioService {
    public Portfolio findByCustomerId(Integer customerId);
    public Portfolio findByCustomerIdElseCreate(Integer customerId);
    public Portfolio savePortfolio(PortfolioInitial portfolioInitial);
    public PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId);
    public Integer getQuantityOfAsset(Integer customerId, String code);
    public Portfolio processBuyTrade(Trade trade);
    public Portfolio processSellTrade(Trade trade);
}
