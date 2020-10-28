package cs203t10.ryver.market.portfolio.asset;

import java.util.List;

public interface AssetService {
    public List<Asset> findByPortfolioCustomerId(Integer customerId);
    public Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code);
    public Integer getQuantityByPortfolioCustomerIdAndCode(Integer customerId, String code);
    public Asset addAsset(Integer customerId, String code, Integer quantity, Double averagePrice);
    public Asset deductFromAsset(Integer customerId, String code, Integer quantity);
    public Asset addToAsset(Integer customerId, String code, Integer quantity, Double unitPrice);
    public void resetAssets();
}