package cs203t10.ryver.market.portfolio.asset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.PortfolioService;
import cs203t10.ryver.market.portfolio.asset.AssetRepository;

@Service
public class AssetServiceImpl implements AssetService{

    @Autowired
    private AssetRepository assets;

    @Autowired
    private PortfolioService portfoliosService;

    @Override
    public List<Asset> findByPortfolioCustomerId(Integer customerId) {
        return assets.findByPortfolioCustomerId(customerId);
    }

    @Override
    public Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code) {
        return assets.findByPortfolioCustomerIdAndCode(customerId, code)
                    .orElseThrow(() -> new StockNotOwnedException(customerId, code));
    }

    @Override
    public Integer getQuantityByPortfolioCustomerIdAndCode(Integer customerId, String code) {
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);
        return asset.getQuantity();
    }

    @Override
    public Asset addAsset(Integer customerId, String code, Integer quantity, Double averagePrice) {
        Portfolio portfolio = portfoliosService.findByCustomerId(customerId);
        Asset asset = Asset.builder().portfolio(portfolio).code(code).quantity(quantity).averagePrice(averagePrice).value(quantity*averagePrice).build();
        return assets.save(asset);
    }

    @Override
    public Asset deductFromAsset(Integer customerId, String code, Integer quantity) {
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);
        Integer newQuantity = asset.getQuantity() - quantity;
        if (newQuantity == 0) {
            assets.delete(asset);
            return null;
        }
        asset.setQuantity(newQuantity);
        asset.setValue(newQuantity*asset.getAveragePrice());
        return assets.save(asset);
    }

    @Override
    public Asset addToAsset(Integer customerId, String code, Integer quantity, Double unitPrice) {
        Asset asset = assets.findByPortfolioCustomerIdAndCode(customerId, code).orElse(null);
        if (asset == null) {
            return addAsset(customerId, code, quantity, unitPrice);
        }
        Integer newQuantity = asset.getQuantity() + quantity;
        Double newValue = asset.getValue() + (quantity*unitPrice);
        Double newAveragePrice = newValue/newQuantity;
        newAveragePrice *= 100;
        newAveragePrice = (double) Math.round(newAveragePrice);
        newAveragePrice /= 100;
        asset.setQuantity(newQuantity);
        asset.setValue(newValue);
        asset.setAveragePrice(newAveragePrice);
        return assets.save(asset);
    }

    @Override
    public void resetAssets(){
        assets.deleteAll();
    }
}
