package cs203t10.ryver.market.portfolio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.Trade;

import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.Asset;
import cs203t10.ryver.market.portfolio.PortfolioAlreadyExistsException;
import cs203t10.ryver.market.portfolio.PortfolioNotFoundException;

@Service
public class PortfolioServiceImpl implements PortfolioService{
    
    @Autowired
    private PortfolioRepository portfolios;
    
    /** 
     * Creates a portfolio for an existing user 
     */
    public Portfolio createPortfolio(Integer customerId) {
        Portfolio portfolio = new Portfolio(customerId, null, 0.0, 0.0);
        try {
            return portfolios.save(portfolio);
        } catch (DataIntegrityViolationException e) {
            throw new PortfolioAlreadyExistsException(customerId);
        }
    }

    /** 
     * Find portfolio belonging to the given customer ID
     */
    public Portfolio findByCustomerId(Integer customerId) {
        return portfolios.findByCustomerId(customerId)
                .orElseThrow(() -> new PortfolioNotFoundException(customerId));
    }

    /** 
     * Find list of assets belonging to the customer ID
     */
    public List<Asset> findAssetsByCustomerId(Integer customerId) {
        Portfolio portfolio = findByCustomerId(customerId);
        return portfolio.getAssets();
    }

    public Asset findAssetByCustomerIdAndCode(Integer customerId, String code) {
        List<Asset> assets = findAssetsByCustomerId(customerId);
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            String assetCode = asset.getCode();
            if (assetCode == code) return asset;
        }
        return null;
    }

    public Portfolio addNewStockToAssets(Integer customerId, Asset asset) {
        String code = asset.getCode();
        Portfolio portfolio = findByCustomerId(customerId);
        Asset newAsset = findAssetByCustomerIdAndCode(customerId, code);
        if (newAsset != null) {
            return portfolio;
        } else {
            List<Asset> assets = portfolio.getAssets();
            assets.add(newAsset);
            portfolio.setAssets(assets);
            return portfolios.save(portfolio);
        }
    }

    public Portfolio addToAssets(Trade trade) {
        String code = trade.getSymbol();
        Integer customerId = trade.getCustomerId();
        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getAvgPrice();

        Portfolio portfolio = findByCustomerId(customerId);
        Asset asset = findAssetByCustomerIdAndCode(customerId, code);

        if (asset == null) {
            asset = new Asset(code, filledQuantity, tradeAvgPrice, 0.0, tradeAvgPrice * filledQuantity, 0.0);
            return addNewAsset(customerId, asset);
        } else {
            Double newAveragePrice = ((tradeAvgPrice * filledQuantity) + asset.getValue())/(asset.getQuantity() + filledQuantity);
            asset.setAveragePrice(newAveragePrice);
            asset.setQuantity(asset.getQuantity() + filledQuantity);
            asset.setValue(asset.getValue() + (tradeAvgPrice * filledQuantity));
        }
        return portfolios.save(portfolio);
    }

    public Portfolio deductFromAssets(Trade trade) {
        String code = trade.getSymbol();
        Integer customerId = trade.getCustomerId();
        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getAvgPrice();

        Portfolio portfolio = findByCustomerId(customerId);
        Asset asset = findAssetByCustomerIdAndCode(customerId, code);

        Integer newQuantity = asset.getQuantity() - filledQuantity;
        Double newValue = asset.getValue() - (filledQuantity * tradeAvgPrice);
        Double newAveragePrice = newValue/newQuantity;
        asset.setAveragePrice(newAveragePrice);
        asset.setQuantity(newQuantity);
        asset.setValue(newValue);

        return portfolios.save(portfolio)
    }

    //update assets currentprice when lastprice on stock in assets changes

}