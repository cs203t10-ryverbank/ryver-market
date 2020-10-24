package cs203t10.ryver.market.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.PortfolioRepository;
import cs203t10.ryver.market.portfolio.PortfolioServiceImpl;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.asset.AssetRepository;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordRepository;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolios;

    @Mock
    private StockRecordRepository stockRecords;

    @Mock
    private AssetRepository assets;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @Test
    public void createPortfolio_NewPortfolio_ReturnsPortfolio() {
        
        Random rand = new Random();
        Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
        Portfolio portfolio = Portfolio.builder()
                                .customerId(testCustomerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        
        when(portfolios.save(any(Portfolio.class))).thenReturn(portfolio);

        Portfolio createdPortfolio = portfolioService.createPortfolio(testCustomerId);

        assertEquals(portfolio, createdPortfolio);
        verify(portfolios).save(portfolio);
    }

    // @Test
    // public void processBuyTrade_NewStock_ExistingPortfolio_ReturnPortfolio() {

    //     Date date = new Date(1602321010000L);
    //     Random rand = new Random();
    //     Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
    //     Portfolio portfolio = Portfolio.builder()
    //                           .customerId(testCustomerId)
    //                           .totalGainLoss(0.0)
    //                           .unrealizedGainLoss(0.0)
    //                           .build();

    //     Stock testStock = new Stock("A1");

    //     StockRecord testStockRecord = StockRecord.builder()
    //                                   .stock(testStock)
    //                                   .submittedDate(date)
    //                                   .price(1.10)
    //                                   .totalVolume(1000000)
    //                                   .build();
        
    //     Trade testTrade = Trade.builder()
    //                   .stock(testStock).action(Action.BUY)
    //                   .quantity(10000).filledQuantity(10000)
    //                   .customerId(testCustomerId).accountId(1)
    //                   .submittedDate(date)
    //                   .status(Status.FILLED).price(1.18).build();
        
    //     Asset asset = Asset.builder()
    //                   .portfolio(portfolio)
    //                   .code("A1")
    //                   .quantity(1000)
    //                   .averagePrice(1.18)
    //                   .currentPrice(1.10)
    //                   .value(11800.0)
    //                   .gainLoss(800.0)
    //                   .build();
        
    //     List<Asset> assetList = new ArrayList<>();
    //     assetList.add(asset);
        
    //     when(stockRecords.findLatestBySymbol(testStock.getSymbol())).thenReturn(Optional.of(testStockRecord));
    //     when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.of(portfolio));
    //     when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.empty());
    //     when(portfolios.save(any(Portfolio.class))).thenReturn(portfolio);
    //     when(portfolios.save(portfolio)).thenReturn(portfolio);

    //     Portfolio updatedPortfolio = portfolioService.processBuyTrade(testTrade);

    //     assertEquals(assetList, updatedPortfolio.getAssets());
    //     assertEquals(800.0, updatedPortfolio.getUnrealizedGainLoss());
    //     assertEquals(800.0, updatedPortfolio.getTotalGainLoss());
    //     verify(stockRecords).findLatestBySymbol(testStock.getSymbol());
    //     verify(portfolios).findByCustomerId(testCustomerId);
    //     verify(portfolios).save(portfolio);
    // }

    // public void processSellTrade_ReturnPortfolio() {

    //     Date date = new Date(1602321010000L);
    //     Random rand = new Random();
    //     Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
    //     Portfolio portfolio = Portfolio.builder()
    //                           .customerId(testCustomerId)
    //                           .totalGainLoss(0.0)
    //                           .unrealizedGainLoss(0.0)
    //                           .build();

    //     Stock testStock = new Stock("A1");

    //     StockRecord testStockRecord = StockRecord.builder()
    //                                   .stock(testStock)
    //                                   .submittedDate(date)
    //                                   .price(1.10)
    //                                   .totalVolume(1000000)
    //                                   .build();
        
    //     Trade testTrade = Trade.builder()
    //                   .stock(testStock).action(Action.SELL)
    //                   .quantity(10000).filledQuantity(10000)
    //                   .customerId(testCustomerId).accountId(1)
    //                   .submittedDate(date)
    //                   .status(Status.FILLED).price(1.18).build();
        
    //     Asset asset = Asset.builder()
    //                   .portfolio(portfolio)
    //                   .code("A1")
    //                   .quantity(100000)
    //                   .averagePrice(1.18)
    //                   .currentPrice(1.10)
    //                   .value(118000.0)
    //                   .gainLoss(8000.0)
    //                   .build();
        
    //     List<Asset> assetList = new ArrayList<>();
    //     assetList.add(asset);

    //     portfolio.setAssets(assetList);
    //     portfolio.setUnrealizedGainLoss(800.0);

    //     when(stockRecords.findLatestBySymbol(testStock.getSymbol())).thenReturn(Optional.of(testStockRecord));
    //     when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.of(portfolio));
    //     when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(asset));
    //     when(portfolios.save(portfolio)).thenReturn(portfolio);
        
    //     Portfolio updatedPortfolio = portfolioService.processSellTrade(testTrade);
        
    //     assertEquals(assetList, updatedPortfolio.getAssets());
    //     assertEquals(800.0, updatedPortfolio.getUnrealizedGainLoss());
    //     assertEquals(800.0, updatedPortfolio.getTotalGainLoss());
    //     verify(stockRecords).findLatestBySymbol(testStock.getSymbol());
    //     verify(portfolios).findByCustomerId(testCustomerId);
    //     verify(portfolios).save(portfolio);

    // }

    // @Test
    // public void calculateUnrealizedGainLoss() {
    //     Random rand = new Random();
    //     Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);


    // }

    // @Test
    // public void processBuyTrade_StockNotCurrentlyOwned_ReturnPortfolio() {
    //     Random rand = new Random();
    //     Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

    //     Asset testAsset1 = Asset.builder()
    //         .code("A17U")
    //         .quantity(1000)
    //         .avgPrice(3.30)
    //         .currentPrice(3.31)
    //         .value(3310.0)
    //         .gainLoss(10.0)
    //         .build();

    //     List<Asset> testAssets = [testAsset1];

    //     Portfolio testPortfolio = Portfolio.builder()
    //         .customerId(testCustomerId)
    //         .assets(testAssets)
    //         .unrealizedGainLoss(10.0)
    //         .totalGainLoss(0.0);

    //     when()
    // }
}