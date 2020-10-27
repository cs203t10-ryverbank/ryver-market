package cs203t10.ryver.market.portfolio.asset;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.portfolio.Portfolio;

public interface AssetService {
    public List<Asset> findByPortfolioCustomerId(Integer customerId);
    public Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code);
    public List<Asset> processBuyTrade(Trade trade, Portfolio portfolio);
    public List<Asset> processSellTrade(Trade trade);
    public List<Asset> updateAssets(Portfolio portfolio);
    public Integer getQuantityOfAsset(Asset asset);
    public void resetAssets();
}