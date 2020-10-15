package cs203t10.ryver.market.portfolio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;

import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.asset.AssetService;
import cs203t10.ryver.market.portfolio.PortfolioAlreadyExistsException;
import cs203t10.ryver.market.portfolio.PortfolioNotFoundException;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolios;

    @Autowired
    private AssetService assetService;

    @Autowired StockRecordService stockRecordService;
    
    /** 
     * Creates a portfolio for an existing user 
     */
    @Override
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
    @Override
    public Portfolio findByCustomerId(Integer customerId) {
        return portfolios.findByCustomerId(customerId)
                .orElseThrow(() -> new PortfolioNotFoundException(customerId));
    }

    @Override
    public Portfolio calculateUnrealizedGainLoss(Portfolio portfolio) {
        List<Asset> assets = portfolio.getAssets();
        Double unrealizedGainLoss = 0.0;
        for (Asset asset : assets) {
            unrealizedGainLoss += asset.getGainLoss();
        }
        portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio updateTotalGainLoss(Portfolio portfolio, Trade trade) {
        Stock stock = trade.getStock();
        String code = stock.getSymbol();
        StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
        Double currentPrice = stockRecord.getPrice();
        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();
        Double gainLoss = filledQuantity * (tradeAvgPrice - currentPrice);
        portfolio.setTotalGainLoss(portfolio.getTotalGainLoss() + gainLoss);
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio processBuyTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = findByCustomerId(customerId);
        List<Asset> asset = assetService.processBuyTrade(trade, portfolio);
        return calculateUnrealizedGainLoss(portfolio);
    }

    @Override
    public Portfolio processSellTrade(Trade trade) {
        List<Asset> asset = assetService.processSellTrade(trade);
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = findByCustomerId(customerId);
        calculateUnrealizedGainLoss(portfolio);
        return updateTotalGainLoss(portfolio, trade);
    }
}
