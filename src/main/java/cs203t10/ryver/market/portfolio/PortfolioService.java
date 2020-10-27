package cs203t10.ryver.market.portfolio;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;

public interface PortfolioService {
    public Portfolio createPortfolio(Integer customerId);
    public PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId);
    public Double calculateUnrealizedGainLoss(Portfolio portfolio);
    public Portfolio processBuyTrade(Trade trade);
    public Portfolio processSellTrade(Trade trade);
    public void resetPortfolios();
}
