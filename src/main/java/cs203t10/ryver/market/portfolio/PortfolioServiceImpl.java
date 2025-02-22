package cs203t10.ryver.market.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.asset.AssetService;
import cs203t10.ryver.market.portfolio.asset.view.AssetInfoViewableByCustomer;
import cs203t10.ryver.market.portfolio.exception.PortfolioAlreadyExistsException;
import cs203t10.ryver.market.portfolio.exception.PortfolioNotFoundException;
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
    private FundTransferService fundTransferService;

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
            Double initialCapital = Double.parseDouble(fundTransferService.getTotalBalance(customerId).getBody());
            Portfolio portfolioInitial = new Portfolio(customerId, assetList, initialCapital);
            return portfolios.save(portfolioInitial);
        } else {
            return portfolio;
        }
    }

    @Override
    public Portfolio savePortfolio(Portfolio portfolioInitial) {
        try {
            return portfolios.save(portfolioInitial);
        } catch (DataIntegrityViolationException e) {
            throw new PortfolioAlreadyExistsException(portfolioInitial.getCustomerId());
        }
    }

    @Override
    public PortfolioInfoViewableByCustomer viewPortfolio(Integer customerId) {
        Portfolio portfolio = findByCustomerIdElseCreate(customerId);
        List<Asset> assetList = assetService.findByPortfolioCustomerId(customerId);
        Double currentCapital = Double.parseDouble(fundTransferService.getTotalBalance(customerId).getBody());

        Double unrealizedGainLoss = 0.0;
        List<AssetInfoViewableByCustomer> assetInfoList = new ArrayList<>();
        Double totalValue = 0.0;
        for (Asset asset : assetList) {
            String code = asset.getCode();
            Integer quantity = asset.getQuantity();
            Double averagePrice = asset.getAveragePrice();
            StockRecord stockRecord = stockRecordService.getLatestStockRecordBySymbol(code);
            Double currentPrice = stockRecord.getPrice();
            Double gainLoss = quantity * (currentPrice - averagePrice);
            AssetInfoViewableByCustomer assetInfoViewableByCustomer = new AssetInfoViewableByCustomer(
                    code, quantity, averagePrice, currentPrice, asset.getValue(), gainLoss);
            assetInfoList.add(assetInfoViewableByCustomer);
            unrealizedGainLoss += gainLoss;
            totalValue += asset.getValue();
        }

        PortfolioInfoViewableByCustomer portfolioInfoViewableByCustomer = new PortfolioInfoViewableByCustomer(
                portfolio.getCustomerId(), assetInfoList, unrealizedGainLoss, currentCapital - portfolio.getInitialCapital() + totalValue);
        return portfolioInfoViewableByCustomer;
    }

    @Override
    public Integer getAvailableQuantityOfAsset(Integer customerId, String code) {
        return assetService.getAvailableQuantityByPortfolioCustomerIdAndCode(customerId, code);
    }

    @Override
    public Portfolio addToInitialCapital(Integer customerId, Double amount) {
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            return null;
        }
        System.out.println(portfolio.getInitialCapital());
        portfolio.setInitialCapital(portfolio.getInitialCapital() + amount);
        System.out.println(portfolio.getInitialCapital());
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio deductFromInitialCapital(Integer customerId, Double amount) {
        Portfolio portfolio = portfolios.findByCustomerId(customerId).orElse(null);
        if (portfolio == null) {
            return null;
        }
        portfolio.setInitialCapital(portfolio.getInitialCapital() - amount);
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio processSellTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = findByCustomerId(customerId);
        String code = trade.getStock().getSymbol();
        Integer filledQuantity = trade.getFilledQuantity();
        assetService.deductFromAsset(customerId, code, filledQuantity);
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio processBuyTrade(Trade trade) {
        Integer customerId = trade.getCustomerId();
        Portfolio portfolio = findByCustomerIdElseCreate(customerId);
        String code = trade.getStock().getSymbol();
        Integer filledQuantity = trade.getFilledQuantity();
        Double unitPrice = trade.getTotalPrice() / filledQuantity;
        assetService.addToAsset(customerId, code, filledQuantity, unitPrice);
        return portfolios.save(portfolio);
    }

    @Override
    public Portfolio registerSellTrade(Integer customerId, String code, Integer quantity) {
        Portfolio portfolio = findByCustomerId(customerId);
        assetService.deductAvailableQuantity(customerId, code, quantity);
        return portfolios.save(portfolio);
    }

    @Override
    public void resetPortfolios() {
        assetService.resetAssets();
        portfolios.deleteAll();
    }

}

