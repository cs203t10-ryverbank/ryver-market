package cs203t10.ryver.market.portfolio.asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.asset.AssetRepository;

@Service
public class AssetServiceImpl implements AssetService{
    
    @Autowired
    private AssetRepository assets;

    @Autowired
    private StockRecordService stockRecordService;

    @Override
    public Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code) {
        return assets.findByPortfolioCustomerIdAndCode(customerId, code)
                    .orElseThrow(() -> new StockNotOwnedException(customerId, code));
    }

    @Override
    public List<Asset> findByPortfolioCustomerId(Integer customerId) {
        return assets.findByPortfolioCustomerId(customerId);
    }

    @Override
    public Asset processBuyTrade(Trade trade, Portfolio portfolio) {
        String code = trade.getStock().getSymbol();
        Integer customerId = trade.getCustomerId();
        Asset asset = assets.findByPortfolioCustomerIdAndCode(customerId, code).orElse(null);

        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();
        
        StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
        Double currentPrice = stockRecord.getPrice();

        if (asset == null) {
            Double value = filledQuantity * tradeAvgPrice;
            Double gainLoss = value - (filledQuantity * currentPrice);
            asset = Asset.builder()
                        .portfolio(portfolio)
                        .code(code)
                        .quantity(filledQuantity)
                        .averagePrice(tradeAvgPrice)
                        .currentPrice(currentPrice)
                        .value(tradeAvgPrice * filledQuantity)
                        .gainLoss(gainLoss)
                        .build();
        } else {
            Integer newQuantity = asset.getQuantity() + filledQuantity;
            Double newValue = asset.getValue() + (tradeAvgPrice * filledQuantity);
            Double newAveragePrice = newValue/newQuantity;
            newAveragePrice *= 100;
            newAveragePrice = (double) Math.round(newAveragePrice);
            newAveragePrice /= 100;
            Double newGainLoss = newValue - (newQuantity * currentPrice);
            newGainLoss *= 100;
            newGainLoss = (double) Math.round(newGainLoss);
            newGainLoss /= 100;

            asset.setAveragePrice(newAveragePrice);
            asset.setQuantity(newQuantity);
            asset.setValue(newValue);
            asset.setGainLoss(newGainLoss);
            asset.setCurrentPrice(currentPrice);
        }
        assets.save(asset);
        return asset;
    }

    @Override
    public Asset processSellTrade(Trade trade) {
        String code = trade.getStock().getSymbol();
        Integer customerId = trade.getCustomerId();
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);
        
        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();

        StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
        Double currentPrice = stockRecord.getPrice();

        Integer newQuantity = asset.getQuantity() - filledQuantity;

        if (newQuantity == 0) {
            assets.delete(asset);
            return null;
        } else {
            Double newValue = asset.getValue() - (filledQuantity * tradeAvgPrice);
            Double newAveragePrice = newValue/newQuantity;
            newAveragePrice *= 100;
            newAveragePrice = (double) Math.round(newAveragePrice);
            newAveragePrice /= 100;
            Double newGainLoss = newValue - (newQuantity * currentPrice); 
            newGainLoss *= 100;
            newGainLoss = (double) Math.round(newGainLoss);
            newGainLoss /= 100;
            asset.setAveragePrice(newAveragePrice);
            asset.setCurrentPrice(currentPrice);
            asset.setQuantity(newQuantity);
            asset.setValue(newValue);
            asset.setGainLoss(newGainLoss);
            assets.save(asset);
            return asset;
        }
    }

    @Override
    public List<Asset> updateAssets (Portfolio portfolio) {
        Integer customerId = portfolio.getCustomerId();
        List<Asset> assetList = assets.findByPortfolioCustomerId(customerId);
        for (Asset asset : assetList) {
            String code = asset.getCode();
            StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
            if (!stockRecord.getPrice().equals(asset.getCurrentPrice())) {
                Double currentPrice = stockRecord.getPrice();
                Double newGainLoss = (asset.getAveragePrice() - currentPrice) * asset.getQuantity();
                newGainLoss *= 100;
                newGainLoss = (double) Math.round(newGainLoss);
                newGainLoss /= 100;

                asset.setCurrentPrice(currentPrice);
                asset.setGainLoss(newGainLoss);
            }
        }
        return portfolio.getAssets();
    }

    @Override
    public Integer getQuantityOfAsset(Asset asset) {
        return asset.getQuantity();
    }
}