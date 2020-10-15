package cs203t10.ryver.market.portfolio;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.portfolio.asset.Asset;

public interface PortfolioService {
    public Portfolio createPortfolio(Integer customerId);
    public Portfolio findByCustomerId(Integer customerId);
    public Portfolio calculateUnrealizedGainLoss(Portfolio portfolio);
    public Portfolio updateTotalGainLoss(Portfolio portfolio, Trade trade);
    public Portfolio processBuyTrade(Trade trade);
    public Portfolio processSellTrade(Trade trade);
}