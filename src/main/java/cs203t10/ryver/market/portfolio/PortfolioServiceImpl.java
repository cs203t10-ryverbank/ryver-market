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
import cs203t10.ryver.market.util.DoubleUtils;

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
        return portfolios.findByCustomerId(customerId).orElseThrow(() -> new PortfolioNotFoundException(customerId));
    }

    @Override
    public Portfolio findByCustomerIdElseCreate(Integer customerId) {
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            List<Asset> assetList = new ArrayList<>();
            PortfolioInitial portfolioInitial = new PortfolioInitial(customerId, assetList, 0.0);
            return portfolios.save(portfolioInitial.toPortfolio());
        } else {
            return portfolio;
        }
    }

    @Override
    public Portfolio savePortfolio(PortfolioInitial portfolioInitial) {
        try {
            return portfolios.save(portfolioInitial.toPortfolio());
        } catch (DataIntegrityViolationException e) {
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
            Double gainLoss = DoubleUtils.getRoundedToNearestCent(quantity * (currentPrice - averagePrice));
            AssetInfoViewableByCustomer assetInfoViewableByCustomer = new AssetInfoViewableByCustomer(
                    code, quantity, averagePrice, currentPrice, asset.getValue(), gainLoss);
            assetInfoList.add(assetInfoViewableByCustomer);
            unrealizedGainLoss += gainLoss;
        }

        PortfolioInfoViewableByCustomer portfolioInfoViewableByCustomer = new PortfolioInfoViewableByCustomer(
                portfolio.getCustomerId(), assetInfoList, unrealizedGainLoss, portfolio.getTotalGainLoss());
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
        Double gainLoss = DoubleUtils.getRoundedToNearestCent(filledQuantity * (sellPrice - currentPrice));
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

    @Override
    public void resetPortfolios() {
        assetService.resetAssets();
        portfolios.deleteAll();
    }

}

