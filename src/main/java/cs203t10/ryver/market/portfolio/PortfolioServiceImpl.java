package cs203t10.ryver.market.portfolio;

import java.util.List;
import java.util.ArrayList;

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
import cs203t10.ryver.market.portfolio.asset.view.AssetInfoViewableByCustomer;
import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;
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
        Portfolio portfolio = Portfolio.builder()
                                .customerId(customerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        try {
            return portfolios.save(portfolio);
        } catch (DataIntegrityViolationException e) {
            throw new PortfolioAlreadyExistsException(customerId);
        }
    }

    @Override
    public PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId) {
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            portfolio = createPortfolio(customerId);
        } else {
            assetService.updateAssets(portfolio);
            Double unrealizedGainLoss = calculateUnrealizedGainLoss(portfolio);
            portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
            portfolio = portfolios.save(portfolio);
        }
        List<Asset> assets = assetService.findByPortfolioCustomerId(customerId);
        List<AssetInfoViewableByCustomer> assetInfoList = new ArrayList<>();
        if (assets != null) {
            for (Asset asset : assets) {
                AssetInfoViewableByCustomer assetInfo = new AssetInfoViewableByCustomer(asset.getCode(),
                                                                                        asset.getQuantity(),
                                                                                        asset.getAveragePrice(),
                                                                                        asset.getCurrentPrice(),
                                                                                        asset.getValue(),
                                                                                        asset.getGainLoss());
                assetInfoList.add(assetInfo);
            }
        }
        PortfolioInfoViewableByCustomer portfolioInfo = new PortfolioInfoViewableByCustomer(portfolio.getCustomerId(),
                                                                        assetInfoList,
                                                                        portfolio.getUnrealizedGainLoss(),
                                                                        portfolio.getTotalGainLoss());
        return portfolioInfo;
    }

    @Override
    public Double calculateUnrealizedGainLoss(Portfolio portfolio) {
        List<Asset> assets = portfolio.getAssets();
        Double unrealizedGainLoss = 0.0;
        for (Asset asset : assets) {
            unrealizedGainLoss += asset.getGainLoss();
        }
        return unrealizedGainLoss;
    }

    @Override
    public Portfolio processBuyTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            portfolio = createPortfolio(customerId);
        }
        assetService.processBuyTrade(trade, portfolio);
        Double unrealizedGainLoss = calculateUnrealizedGainLoss(portfolio);
        portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio processSellTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            portfolio = createPortfolio(customerId);
        }
        String code = trade.getStock().getSymbol();
        Asset asset = assetService.findByPortfolioCustomerIdAndCode(customerId, code);

        assetService.processSellTrade(trade);

        Double unrealizedGainLoss = calculateUnrealizedGainLoss(portfolio);

        Integer filledQuantity = trade.getFilledQuantity();
        Double tradeAvgPrice = trade.getPrice();
        Double tradeGainLoss = filledQuantity * (tradeAvgPrice - asset.getAveragePrice());

        portfolio.setTotalGainLoss(portfolio.getTotalGainLoss() + tradeGainLoss);
        portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
        return portfolios.save(portfolio);
    }
}
