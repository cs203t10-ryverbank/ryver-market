package cs203t10.ryver.market.portfolio.asset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.PortfolioService;
import cs203t10.ryver.market.portfolio.asset.exception.StockNotOwnedException;

@Service
public class AssetServiceImpl implements AssetService {

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
    public Integer getAvailableQuantityByPortfolioCustomerIdAndCode(Integer customerId, String code) {
        try {
            Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);
            return asset.getAvailableQuantity();
        } catch (StockNotOwnedException e) {
            return 0;
        }
    }

    @Override
    public Asset addAsset(Integer customerId, String code, Integer quantity, Double value) {
        Portfolio portfolio = portfoliosService.findByCustomerId(customerId);
        Asset asset = Asset.builder()
            .portfolio(portfolio)
            .code(code)
            .quantity(quantity)
            .availableQuantity(quantity)
            .value(value)
            .build();
        return assets.save(asset);
    }

    @Override
    public Asset deductFromAsset(Integer customerId, String code, Integer quantity) {
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);
        Integer newQuantity = asset.getQuantity() - quantity;
        Double avePrice = asset.getValue() / asset.getQuantity();
        Double newValue = newQuantity * avePrice;
        if (newQuantity == 0) {
            assets.delete(asset);
            return null;
        }
        asset.setQuantity(newQuantity);
        asset.setAvailableQuantity(asset.getAvailableQuantity() - quantity);
        asset.setValue(newValue);
        return assets.save(asset);
    }

    @Override
    public Asset deductAvailableQuantity(Integer customerId, String code, Integer quantity) {
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);
        asset.setAvailableQuantity(asset.getAvailableQuantity() - quantity);
        return assets.save(asset);
    }

    @Override
    public Asset addToAsset(Integer customerId, String code,
            Integer quantity, Double unitPrice) {
        Asset asset = assets.findByPortfolioCustomerIdAndCode(customerId, code).orElse(null);
        if (asset == null) {
            return addAsset(customerId, code, quantity, quantity * unitPrice);
        }
        Integer newQuantity = asset.getQuantity() + quantity;
        Integer newAvailableQuantity = asset.getAvailableQuantity() + quantity;
        Double newValue = asset.getValue() + (quantity * unitPrice);
        asset.setQuantity(newQuantity);
        asset.setAvailableQuantity(newAvailableQuantity);
        asset.setValue(newValue);
        return assets.save(asset);
    }

    @Override
    public void resetAssetAvailableQuantity() {
        List<Asset> assetList = assets.findAll();
        for (Asset asset : assetList) {
            asset.setAvailableQuantity(asset.getQuantity());
            assets.save(asset);
        }
    }

    @Override
    public void resetAssets() {
        assets.deleteAll();
    }
}

