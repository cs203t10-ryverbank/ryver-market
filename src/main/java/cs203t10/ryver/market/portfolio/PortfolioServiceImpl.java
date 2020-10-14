package cs203t10.ryver.market.portfolio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.Trade;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolios;

    @Autowired
    private StockRecordService stockService;

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

    /**
     * Find list of asset with a particular stock symbol belonging to the customer ID
     */
    public Asset findAssetByCustomerIdAndCode(Integer customerId, String code) {
        List<Asset> assets = findAssetsByCustomerId(customerId);
        for (Asset asset : assets) {
            String assetCode = asset.getCode();
            if (assetCode == code) return asset;
        }
        return null;
    }

    /**
     * Add a stock which customer does not currently own to the assets list
     */
    public Portfolio addNewStockToAssets(Integer customerId, Asset asset) {
        String code = asset.getCode();
        Portfolio portfolio = findByCustomerId(customerId);
        Asset newAsset = findAssetByCustomerIdAndCode(customerId, code);
        if (newAsset != null) {
            return portfolio;
        }
        List<Asset> assets = portfolio.getAssets();
        assets.add(newAsset);
        portfolio.setAssets(assets);
        return portfolios.save(portfolio);
    }

    /**
     * Process a buy trade
     */
    public Portfolio addToAssets(Trade trade) {
        String code = trade.getStock().getSymbol();
        StockRecord stockRecord = stockService.getLatestStockRecordBySymbol(code);
        Integer customerId = trade.getCustomerId();
        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();
        Double currentPrice = stockRecord.getPrice();

        Portfolio portfolio = findByCustomerId(customerId);
        Asset asset = findAssetByCustomerIdAndCode(customerId, code);

        // TODO: Better documentation and better naming of method.
        // Each function should handle only one layer of abstraction.
        if (asset == null) {
            Double gainLoss = (filledQuantity * tradeAvgPrice) - (filledQuantity * currentPrice);
            asset = new Asset(code, filledQuantity, tradeAvgPrice, currentPrice, tradeAvgPrice * filledQuantity, gainLoss);
            return addNewStockToAssets(customerId, asset);
        }

        Integer newQuantity = asset.getQuantity() + filledQuantity;
        Double newValue = asset.getValue() + (tradeAvgPrice * filledQuantity);
        Double newAveragePrice = newValue/newQuantity;
        Double newGainLoss = newValue - (newQuantity * currentPrice);

        asset.setAveragePrice(newAveragePrice);
        asset.setQuantity(newQuantity);
        asset.setValue(newValue);
        asset.setGainLoss(newGainLoss);
        return portfolios.save(portfolio);
    }

    /**
     * Process a sell trade
     */
    public Portfolio deductFromAssets(Trade trade) {
        String code = trade.getStock().getSymbol();
        StockRecord stockRecord = stockService.getLatestStockRecordBySymbol(code);
        Integer customerId = trade.getCustomerId();
        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();
        Double currentPrice = stockRecord.getPrice();

        Portfolio portfolio = findByCustomerId(customerId);
        Asset asset = findAssetByCustomerIdAndCode(customerId, code);

        Integer newQuantity = asset.getQuantity() - filledQuantity;
        Double newValue = asset.getValue() - (filledQuantity * tradeAvgPrice);
        Double newAveragePrice = newValue/newQuantity;
        Double newGainLoss = newValue - (newQuantity * currentPrice);

        asset.setAveragePrice(newAveragePrice);
        asset.setQuantity(newQuantity);
        asset.setValue(newValue);
        asset.setGainLoss(newGainLoss);
        return portfolios.save(portfolio);
    }

    /**
     * Update the current_price field in asset when a new trade is made and the last_price of a stock changes
     */
    public Portfolio updateCurrentPrice(StockRecord stockRecord) {
        // Update assets currentprice when lastprice on stock in assets changes
        return null;
    }

}
