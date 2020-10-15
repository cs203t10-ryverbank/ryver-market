package cs203t10.ryver.market.portfolio.asset;

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

    public Asset addAssetRecord(Asset asset) {
        return assets.save(asset);
    }

    public List<Asset> findByPortfolioCustomerId(Integer customerId) {
        return assets.findByPortfolioCustomerId(customerId);
    }

    public List<Asset> findByCode(String code) {
        return assets.findByCode(code);
    }

    public Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code) {
        return assets.findByPortfolioCustomerIdAndCode(customerId, code);
    }

    public List<Asset> processBuyTrade(Trade trade, Portfolio portfolio) {
        String code = trade.getStock().getSymbol();
        Integer customerId = trade.getCustomerId();
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);

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
            Double newGainLoss = newValue - (newQuantity * currentPrice);

            asset.setAveragePrice(newAveragePrice);
            asset.setQuantity(newQuantity);
            asset.setValue(newValue);
            asset.setGainLoss(newGainLoss);
        }
        assets.save(asset);
        return findByPortfolioCustomerId(customerId);
    }

    public List<Asset> processSellTrade(Trade trade) {
        String code = trade.getStock().getSymbol();
        Integer customerId = trade.getCustomerId();
        Asset asset = findByPortfolioCustomerIdAndCode(customerId, code);

        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();

        StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
        Double currentPrice = stockRecord.getPrice();

        Integer newQuantity = asset.getQuantity() - filledQuantity;
        Double newValue = asset.getValue() - (filledQuantity * tradeAvgPrice);
        Double newAveragePrice = newValue/newQuantity;
        Double newGainLoss = newValue - (newQuantity * currentPrice); 

        if (newQuantity == 0) {
            assets.deleteByPortfolioCustomerIdAndCode(customerId, code);
        } else {
            asset.setAveragePrice(newAveragePrice);
            asset.setQuantity(newQuantity);
            asset.setValue(newValue);
            asset.setGainLoss(newGainLoss);
        }
        assets.save(asset);
        return assets.findByPortfolioCustomerId(customerId);
    }

    public List<Asset> updateCurrentPrice(StockRecord stockRecord) {
        String code = stockRecord.getStock().getSymbol();
        Double currentPrice = stockRecord.getPrice();
        List<Asset> assetList = assets.findByCode(code);
        for (Asset asset : assetList) {
            asset.setCurrentPrice(currentPrice);
            assets.save(asset);
        }
        return assets.findByCode(code);
    }
}