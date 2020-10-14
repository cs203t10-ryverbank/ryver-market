package cs203t10.ryver.market.portfolio;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;

public interface PortfolioService {
    Portfolio createPortfolio(Integer customerId);
    Portfolio findByCustomerId(Integer customerId);
    List<Asset> findAssetsByCustomerId(Integer customerId);
    Asset findAssetByCustomerIdAndCode(Integer customerId, String code);
    Portfolio addNewStockToAssets(Integer customerId, Asset asset);
    Portfolio addToAssets(Trade trade);
    Portfolio deductFromAssets(Trade trade);
}

