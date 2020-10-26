package cs203t10.ryver.market.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.asset.AssetService;
import cs203t10.ryver.market.portfolio.asset.view.AssetInfoViewableByCustomer;
import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolios;

    @Autowired
    private AssetService assetService;

    @Autowired
    private StockRecordService stockRecordService;

    @Override
    public Portfolio findByCustomerId(Integer customerId) {
        return portfolios.findByCustomerId(customerId).orElse(null);
    }

    @Override
    public Portfolio findByCustomerIdElseCreate(Integer customerId) {
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            List<Asset> assetList = new ArrayList<>();
            PortfolioInitial portfolioInitial = new PortfolioInitial(customerId, assetList, 0.0);
            return savePortfolio(portfolioInitial);
        } else return portfolio;
    }

    @Override
    public Portfolio savePortfolio(PortfolioInitial portfolioInitial) {
        try {
			return portfolios.save(portfolioInitial.toPortfolio());
		}
		catch (DataIntegrityViolationException e) {
			throw new PortfolioAlreadyExistsException(portfolioInitial.getCustomerId());
		}
    }

    @Override
    public PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId) {
        Portfolio portfolio = findByCustomerIdElseCreate(customerId);
        List<Asset> assetList = assetService.findByPortfolioCustomerId(customerId);

        Double unrealizedGainLoss = 0.0;
        List<AssetInfoViewableByCustomer> assetInfoList = new ArrayList<>();

        for (Asset asset : assetList) {
            String code = asset.getCode();
            Integer quantity = asset.getQuantity();
            Double averagePrice = asset.getAveragePrice();
            StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
            Double currentPrice = stockRecord.getPrice();
            Double gainLoss = quantity * (currentPrice - averagePrice);
            gainLoss *= 100;
            gainLoss = (double) Math.round(gainLoss);
            gainLoss /= 100;
            AssetInfoViewableByCustomer assetInfoViewableByCustomer = new AssetInfoViewableByCustomer(code, quantity, averagePrice, currentPrice, asset.getValue(), gainLoss);
            assetInfoList.add(assetInfoViewableByCustomer);
            unrealizedGainLoss += gainLoss;
        }

        PortfolioInfoViewableByCustomer portfolioInfoViewableByCustomer = new PortfolioInfoViewableByCustomer(portfolio.getCustomerId(), assetInfoList, unrealizedGainLoss, portfolio.getTotalGainLoss());
        return portfolioInfoViewableByCustomer; 
    }

    @Override
    public Integer getQuantityOfAsset(Integer customerId, String code) {
        return assetService.getQuantityByPortfolioCustomerIdAndCode(customerId, code);
    }

    public Portfolio processSellTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = findByCustomerIdElseCreate(customerId);
        String code = trade.getStock().getSymbol();
        Integer filledQuantity = trade.getFilledQuantity();
        assetService.deductFromAsset(customerId, code, filledQuantity);
        
        StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
        Double currentPrice = stockRecord.getPrice();
        Double sellPrice = trade.getPrice();
        Double gainLoss = filledQuantity * (sellPrice - currentPrice);
        portfolio.setTotalGainLoss(portfolio.getTotalGainLoss() + gainLoss);
        return portfolios.save(portfolio);
    }

    public Portfolio processBuyTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = findByCustomerIdElseCreate(customerId);
        String code = trade.getStock().getSymbol();
        Integer filledQuantity = trade.getFilledQuantity();
        Double unitPrice = trade.getPrice();
        assetService.addToAsset(customerId, code, filledQuantity, unitPrice);
        return portfolios.save(portfolio);
    }
}
//     public List<Asset> updateAssets(Portfolio portfolio) {
//         List<Asset> assetList = portfolio.getAssets();
//         if (!assetList.isEmpty()) {
//             for (Asset asset : assetList) {
//                 String code = asset.getCode();
//                 StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
//                 Double currentPrice = stockRecord.getPrice();
//                 Double newGainLoss = (currentPrice - asset.getAveragePrice()) * asset.getQuantity();
//                 newGainLoss *= 100;
//                 newGainLoss = (double) Math.round(newGainLoss);
//                 newGainLoss /= 100;
//                 asset.setCurrentPrice(currentPrice);
//                 asset.setGainLoss(newGainLoss);
//             }
//         }
//         return assetList;
//     }

//     @Override
//     public PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId) {
//         Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
//         if (portfolio == null) {
//             portfolio = createPortfolio(customerId);
//         } else {
//             List<Asset> updatedAssetList = updateAssets(portfolio);
//             Double unrealizedGainLoss = calculateUnrealizedGainLoss(portfolio);
//             portfolio.setAssets(updatedAssetList);
//             portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
//             portfolio = portfolios.save(portfolio);
//         }
//         PortfolioInfoViewableByCustomer portfolioInfo = new PortfolioInfoViewableByCustomer(portfolio.getCustomerId(),
//                                                                         portfolio.getAssets(),
//                                                                         portfolio.getUnrealizedGainLoss(),
//                                                                         portfolio.getTotalGainLoss());
//         return portfolioInfo;
//     }

//     @Override
//     public Double calculateUnrealizedGainLoss(List<Asset> assetList) {
//         Double unrealizedGainLoss = 0.0;
//         if (!assetList.isEmpty()){
//             for (Asset asset : assetList) {
//                 unrealizedGainLoss += asset.getGainLoss();
//             }
//         }
//         return unrealizedGainLoss;
//     }

//     @Override
//     public Portfolio processBuyTrade(Trade trade) {
//         Integer customerId = trade.getCustomerId();
//         Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
//         if (portfolio == null) {
//             portfolio = createPortfolio(customerId);
//         }
//         assetService.processBuyTrade(trade, portfolio);
//         Double unrealizedGainLoss = calculateUnrealizedGainLoss(portfolio);
//         portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
//         return portfolios.save(portfolio);
//     }

//     @Override
//     public Portfolio processSellTrade(Trade trade) {
//         Integer customerId = trade.getCustomerId();
//         Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
//         if (portfolio == null) {
//             portfolio = createPortfolio(customerId);
//         }

//         Asset asset = assetService.processSellTrade(trade);
//         Double unrealizedGainLoss = calculateUnrealizedGainLoss(portfolio);

//         Integer filledQuantity = trade.getFilledQuantity();
//         Double tradeAvgPrice = trade.getPrice();
//         Double tradeGainLoss = filledQuantity * (tradeAvgPrice - asset.getAveragePrice());

//         portfolio.setTotalGainLoss(portfolio.getTotalGainLoss() + tradeGainLoss);
//         portfolio.setUnrealizedGainLoss(unrealizedGainLoss);
//         return portfolios.save(portfolio);
//     }
// }
