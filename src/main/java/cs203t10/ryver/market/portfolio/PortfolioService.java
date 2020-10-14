package cs203t10.ryver.market.portfolio;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.portfolio.Asset;

public interface PortfolioService {
    public Portfolio createPortfolio(Integer customerId);
    public Portfolio findByCustomerId(Integer customerId);
    public List<Asset> findAssetsByCustomerId(Integer customerId);
    public Asset findAssetByCustomerIdAndCode(Integer customerId, String code);
    public Portfolio addNewStockToAssets(Integer customerId, Asset asset);
    public Portfolio addToAssets(Trade trade);
    public Portfolio deductFromAssets(Trade trade);
}