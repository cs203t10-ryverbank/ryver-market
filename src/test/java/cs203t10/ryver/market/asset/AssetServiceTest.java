package cs203t10.ryver.market.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.asset.AssetRepository;
import cs203t10.ryver.market.portfolio.asset.AssetServiceImpl;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock 
    private AssetRepository assets;

    @Mock
    private StockRecordService stockRecordService;

    @InjectMocks
    private AssetServiceImpl assetService;

    @Test
    public void processBuyTrade_NotOwned_ReturnAsset() {
        Random rand = new Random();
        Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
        Portfolio portfolio = Portfolio.builder()
                                .customerId(testCustomerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        
        Date date = new Date(1602321010000L);
        Stock testStock = new Stock("A1");

        StockRecord testStockRecord = StockRecord.builder()
                                      .stock(testStock)
                                      .submittedDate(date)
                                      .price(1.10)
                                      .totalVolume(1000000)
                                      .build();
                                    
        Trade testTrade = Trade.builder()
                          .stock(testStock).action(Action.BUY)
                          .quantity(10000).filledQuantity(10000)
                          .customerId(testCustomerId).accountId(1)
                          .submittedDate(date)
                          .status(Status.FILLED).price(1.18).build();
        
        Asset addedAsset = Asset.builder()
                     .portfolio(portfolio)
                     .code("A1")
                     .quantity(10000)
                     .averagePrice(1.18)
                     .currentPrice(1.10)
                     .value(11800.0)
                     .gainLoss(800.0)
                     .build();

        when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.empty());
        when(assets.save(addedAsset)).thenReturn(addedAsset);
        when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);


        Asset updatedAsset = assetService.processBuyTrade(testTrade, portfolio);

        assertEquals(addedAsset, updatedAsset);
        verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
        verify(assets).save(addedAsset);
        verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
    }

    @Test
    public void processBuyTrade_Owned_NeedToUpdateCurrentPrice_ReturnAsset() {
        Random rand = new Random();
        Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
        // Mock portfolio
        Portfolio portfolio = Portfolio.builder()
                                .customerId(testCustomerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        
        Date date = new Date(1602321010000L);
        Stock testStock = new Stock("A1");

        //Mock stockRecord to return
        StockRecord testStockRecord = StockRecord.builder()
                                      .stock(testStock)
                                      .submittedDate(date)
                                      .price(1.11)
                                      .totalVolume(1000000)
                                      .build();
        
        // Mock trade
        Trade testTrade = Trade.builder()
                          .stock(testStock).action(Action.BUY)
                          .quantity(10000).filledQuantity(10000)
                          .customerId(testCustomerId).accountId(1)
                          .submittedDate(date)
                          .status(Status.FILLED).price(1.18).build();
        
        // Mock the asset record which user currently owns
        Asset oldAsset = Asset.builder()
                     .portfolio(portfolio)
                     .code("A1")
                     .quantity(10000)
                     .averagePrice(1.12)
                     .currentPrice(1.10)
                     .value(11200.0)
                     .gainLoss(200.0)
                     .build();
        
        List<Asset> oldAssetList = new ArrayList<>();
        oldAssetList.add(oldAsset);

        // New asset record after user completes buy trade
        Asset newAsset = Asset.builder()
                         .portfolio(portfolio)
                         .code("A1")
                         .quantity(20000)
                         .averagePrice(1.15)
                         .currentPrice(1.11)
                         .value(23000.0)
                         .gainLoss(800.0)
                         .build();

        when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(oldAsset));
        when(assets.save(newAsset)).thenReturn(newAsset);
        when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);
        
        Asset updatedAsset = assetService.processBuyTrade(testTrade, portfolio);

        assertEquals(newAsset, updatedAsset);
        verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
        verify(assets).save(newAsset);
        verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
       
    }

    @Test
    public void processSellTrade_QuantityBecomesZero_ReturnNull() {
        Random rand = new Random();
        Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
        // Mock portfolio
        Portfolio portfolio = Portfolio.builder()
                                .customerId(testCustomerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        
        Date date = new Date(1602321010000L);
        Stock testStock = new Stock("A1");

        //Mock stockRecord to return
        StockRecord testStockRecord = StockRecord.builder()
                                      .stock(testStock)
                                      .submittedDate(date)
                                      .price(1.11)
                                      .totalVolume(1000000)
                                      .build();
        
        // Mock trade
        Trade testTrade = Trade.builder()
                          .stock(testStock).action(Action.SELL)
                          .quantity(10000).filledQuantity(10000)
                          .customerId(testCustomerId).accountId(1)
                          .submittedDate(date)
                          .status(Status.FILLED).price(1.18).build();
        
        // Mock the asset record which user currently owns
        Asset oldAsset = Asset.builder()
                     .portfolio(portfolio)
                     .code("A1")
                     .quantity(10000)
                     .averagePrice(1.12)
                     .currentPrice(1.10)
                     .value(11200.0)
                     .gainLoss(200.0)
                     .build();

        when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(oldAsset));
        when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);

        Asset updatedAsset = assetService.processSellTrade(testTrade);

        assertNull(updatedAsset);
        verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
        verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
    }

    @Test
    public void processSellTrade_NewQuantityNotZero_NeedToUpdateCurrentPrice_ReturnAsset() {
        Random rand = new Random();
        Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
        // Mock portfolio
        Portfolio portfolio = Portfolio.builder()
                                .customerId(testCustomerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        
        Date date = new Date(1602321010000L);
        Stock testStock = new Stock("A1");

        //Mock stockRecord to return
        StockRecord testStockRecord = StockRecord.builder()
                                      .stock(testStock)
                                      .submittedDate(date)
                                      .price(1.11)
                                      .totalVolume(1000000)
                                      .build();
        
        // Mock trade
        Trade testTrade = Trade.builder()
                          .stock(testStock).action(Action.SELL)
                          .quantity(5000).filledQuantity(5000)
                          .customerId(testCustomerId).accountId(1)
                          .submittedDate(date)
                          .status(Status.FILLED).price(1.18).build();
        
        // Mock the asset record which user currently owns
        Asset oldAsset = Asset.builder()
                     .portfolio(portfolio)
                     .code("A1")
                     .quantity(10000)
                     .averagePrice(1.12)
                     .currentPrice(1.10)
                     .value(11200.0)
                     .gainLoss(200.0)
                     .build();
        
        List<Asset> oldAssetList = new ArrayList<>();
        oldAssetList.add(oldAsset);

        // New asset record after user completes sell trade
        Asset newAsset = Asset.builder()
                         .portfolio(portfolio)
                         .code("A1")
                         .quantity(5000)
                         .averagePrice(1.06)
                         .currentPrice(1.11)
                         .value(5300.0)
                         .gainLoss(-250.0)
                         .build();

        when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(oldAsset));
        when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);
        when(assets.save(newAsset)).thenReturn(newAsset);

        Asset updatedAsset = assetService.processSellTrade(testTrade);

        assertEquals(newAsset, updatedAsset);
        verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
        verify(assets).save(newAsset);
        verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
    }

    @Test public void updateAssets_ReturnListOfUpdatedAssets() {
        Random rand = new Random();
        Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
        // Mock portfolio
        Portfolio portfolio = Portfolio.builder()
                                .customerId(testCustomerId)
                                .totalGainLoss(0.0)
                                .unrealizedGainLoss(0.0)
                                .build();
        
        Date date = new Date(1602321010000L);
        Stock testStock1 = new Stock("A1");
        Stock testStock2 = new Stock("A2");

        //Mock stockRecord to return
        StockRecord testStockRecord1 = StockRecord.builder()
                                      .stock(testStock1)
                                      .submittedDate(date)
                                      .price(1.11)
                                      .totalVolume(1000000)
                                      .build();
        StockRecord testStockRecord2 = StockRecord.builder()
                                      .stock(testStock2)
                                      .submittedDate(date)
                                      .price(1.11)
                                      .totalVolume(1000000)
                                      .build();
        
        // Mock the asset record which user currently owns
        Asset testAsset1 = Asset.builder()
                         .portfolio(portfolio)
                         .code("A1")
                         .quantity(10000)
                         .averagePrice(1.12)
                         .currentPrice(1.10)
                         .value(11200.0)
                         .gainLoss(200.0)
                         .build();
        Asset testAsset2 = Asset.builder()
                         .portfolio(portfolio)
                         .code("A2")
                         .quantity(10000)
                         .averagePrice(1.12)
                         .currentPrice(1.15)
                         .value(11200.0)
                         .gainLoss(-30.0)
                         .build();
        
        List<Asset> testAssetList = new ArrayList<>();
        testAssetList.add(testAsset1);
        testAssetList.add(testAsset2);
        portfolio.setAssets(testAssetList);

        Asset newAsset1 = Asset.builder()
                         .portfolio(portfolio)
                         .code("A1")
                         .quantity(10000)
                         .averagePrice(1.12)
                         .currentPrice(1.11)
                         .value(11200.0)
                         .gainLoss(100.0)
                         .build();
        Asset newAsset2 = Asset.builder()
                         .portfolio(portfolio)
                         .code("A2")
                         .quantity(10000)
                         .averagePrice(1.12)
                         .currentPrice(1.11)
                         .value(11200.0)
                         .gainLoss(100.0)
                         .build();

        List<Asset> newAssetList = new ArrayList<>();
        newAssetList.add(newAsset1);
        newAssetList.add(newAsset2);
        
        when(assets.findByPortfolioCustomerId(testCustomerId)).thenReturn(testAssetList);
        when(stockRecordService.getLatestStockRecordBySymbol(testStock1.getSymbol())).thenReturn(testStockRecord1);
        when(stockRecordService.getLatestStockRecordBySymbol(testStock2.getSymbol())).thenReturn(testStockRecord2);

        List<Asset> updatedAssetList = assetService.updateAssets(portfolio);

        assertEquals(newAssetList, updatedAssetList);
        verify(assets).findByPortfolioCustomerId(testCustomerId);
        verify(stockRecordService).getLatestStockRecordBySymbol(testStock1.getSymbol());
        verify(stockRecordService).getLatestStockRecordBySymbol(testStock2.getSymbol());
    }
}

//     @Test
//     public void addAssetRecord_ValidAssetRecord_ChangesUnrealizedGainLoss() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         Portfolio testPortfolio = Portfolio.builder()
//             .customerId(testCustomerId)
//             .unrealizedGainLoss(0.0)
//             .totalGainLoss(0.0);
        
//         Asset asset = Asset.builder()
//             .portfolio(testPortfolio)
//             .code("A17U")
//             .quantity(1000)
//             .avgPrice(3.30)
//             .currentPrice(3.31)
//             .value(3310.0)
//             .gainLoss(10.0)
//             .build();
        
//         when(assets.save(asset)).thenReturn(asset);

//         Asset addedAsset = assetService.addAssetRecord(asset);

//         assertEquals(10.0, portfolio.getUnrealizedGainLoss());
//     }

//     @Test
//     public processBuyTrade_ValidTrade_ChangesUnrealizedGainLoss() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         Portfolio testPortfolio = Portfolio.builder()
//             .customerId(testCustomerId)
//             .unrealizedGainLoss(0.0)
//             .totalGainLoss(0.0);
        
//         Trade trade = Trade.builder()
//             .action("buy")
//             .symbol("A17U")
//             .quantity(1000)
//             .price(3.30)
//             .filledQuantity(1000)
//             .
//         Asset testAsset = Asset.builder()
//             .portfolio(testPortfolio)
//             .code("A17U")
//             .quantity(1000)
//             .avgPrice(3.30)
//             .currentPrice(3.31)
//             .value(3310.0)
//             .gainLoss(10.0)
//             .build();
        
//         List<Asset> assets = new ArrayList<Asset>();
//         assets.add(TestAsset)
//     }