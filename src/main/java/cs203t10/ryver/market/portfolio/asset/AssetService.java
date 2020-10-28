package cs203t10.ryver.market.portfolio.asset;

import java.util.List;

public interface AssetService {
    List<Asset> findByPortfolioCustomerId(Integer customerId);
    Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code);
    Integer getQuantityByPortfolioCustomerIdAndCode(Integer customerId, String code);
    Asset addAsset(Integer customerId, String code, Integer quantity, Double averagePrice);
    Asset deductFromAsset(Integer customerId, String code, Integer quantity);
    Asset addToAsset(Integer customerId, String code, Integer quantity, Double unitPrice);
    void resetAssets();
}

