// package cs203t10.ryver.market.asset;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.sql.Date;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.Random;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import cs203t10.ryver.market.portfolio.Portfolio;
// import cs203t10.ryver.market.portfolio.asset.Asset;
// import cs203t10.ryver.market.portfolio.asset.AssetRepository;
// import cs203t10.ryver.market.portfolio.asset.AssetServiceImpl;
// import cs203t10.ryver.market.stock.Stock;
// import cs203t10.ryver.market.stock.StockRecord;
// import cs203t10.ryver.market.stock.StockRecordService;
// import cs203t10.ryver.market.trade.Trade;
// import cs203t10.ryver.market.trade.Trade.Action;
// import cs203t10.ryver.market.trade.Trade.Status;

// @ExtendWith(MockitoExtension.class)
// public class AssetServiceTest {

//     @Mock 
//     private AssetRepository assets;

//     @Mock
//     private StockRecordService stockRecordService;

//     @InjectMocks
//     private AssetServiceImpl assetService;

//     @Test
//     public void processBuyTrade_NotOwned_ReturnAsset() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
//         Portfolio portfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .totalGainLoss(0.0)
//                                 .unrealizedGainLoss(0.0)
//                                 .build();
        
//         Date date = new Date(1602321010000L);
//         Stock testStock = new Stock("A1");

//         StockRecord testStockRecord = StockRecord.builder()
//                                       .stock(testStock)
//                                       .submittedDate(date)
//                                       .price(1.10)
//                                       .totalVolume(1000000)
//                                       .build();
                                    
//         Trade testTrade = Trade.builder()
//                           .stock(testStock).action(Action.BUY)
//                           .quantity(10000).filledQuantity(10000)
//                           .customerId(testCustomerId).accountId(1)
//                           .submittedDate(date)
//                           .status(Status.FILLED).price(1.18).build();
        
//         Asset addedAsset = Asset.builder()
//                      .portfolio(portfolio)
//                      .code("A1")
//                      .quantity(10000)
//                      .averagePrice(1.18)
//                      .currentPrice(1.10)
//                      .value(11800.0)
//                      .gainLoss(800.0)
//                      .build();

//         when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.empty());
//         when(assets.save(addedAsset)).thenReturn(addedAsset);
//         when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);


//         Asset updatedAsset = assetService.processBuyTrade(testTrade, portfolio);

//         assertEquals(addedAsset, updatedAsset);
//         verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
//         verify(assets).save(addedAsset);
//         verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
//     }

//     @Test
//     public void processBuyTrade_Owned_NeedToUpdateCurrentPrice_ReturnAsset() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         List<Asset> testAssetList = new ArrayList<>();

//         Portfolio testPortfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .totalGainLoss(0.0)
//                                 .unrealizedGainLoss(-200.0)
//                                 .build();

//         Date date = new Date(1602321010000L);
//         Stock testStock = new Stock("TEST");

//         //Mock stockRecord to return
//         StockRecord testStockRecord = StockRecord.builder()
//                                       .stock(testStock)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();
        
//         // Mock trade
//         Trade testTrade = Trade.builder()
//                           .stock(testStock).action(Action.BUY)
//                           .quantity(10000).filledQuantity(10000)
//                           .customerId(testCustomerId).accountId(1)
//                           .submittedDate(date)
//                           .status(Status.FILLED).price(1.18).build();
        
//         // Mock the asset record which user currently owns
//         Asset testAsset = Asset.builder()
//                      .portfolio(testPortfolio)
//                      .code("TEST")
//                      .quantity(10000)
//                      .averagePrice(1.12)
//                      .currentPrice(1.10)
//                      .value(11200.0)
//                      .gainLoss(-200.0)
//                      .build();
        
//         testAssetList.add(testAsset);

//         // // New asset record after user completes buy trade
//         // Asset updatedAsset = Asset.builder()
//         //                     .portfolio(updatedPortfolio)
//         //                     .code("TEST")
//         //                     .quantity(20000)
//         //                     .averagePrice(1.15)
//         //                     .currentPrice(1.11)
//         //                     .value(23000.0)
//         //                     .gainLoss(-800.0)
//         //                     .build();

//         when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(testAsset));
//         when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);
        
//         Asset returnedAsset = assetService.processBuyTrade(testTrade, testPortfolio);

//         assertEquals(20000, returnedAsset.getQuantity());
//         assertEquals(1.15, returnedAsset.getAveragePrice());
//         assertEquals(1.11, returnedAsset.getCurrentPrice());
//         assertEquals(23000.0, returnedAsset.getValue());
//         assertEquals(-800.0, returnedAsset.getGainLoss());
//         verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
//         verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
//     }

//     @Test
//     public void processSellTrade_QuantityBecomesZero_ReturnNull() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
//         // Mock portfolio
//         Portfolio portfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .totalGainLoss(0.0)
//                                 .unrealizedGainLoss(0.0)
//                                 .build();
        
//         Date date = new Date(1602321010000L);
//         Stock testStock = new Stock("A1");

//         //Mock stockRecord to return
//         StockRecord testStockRecord = StockRecord.builder()
//                                       .stock(testStock)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();
        
//         // Mock trade
//         Trade testTrade = Trade.builder()
//                           .stock(testStock).action(Action.SELL)
//                           .quantity(10000).filledQuantity(10000)
//                           .customerId(testCustomerId).accountId(1)
//                           .submittedDate(date)
//                           .status(Status.FILLED).price(1.18).build();
        
//         // Mock the asset record which user currently owns
//         Asset oldAsset = Asset.builder()
//                      .portfolio(portfolio)
//                      .code("A1")
//                      .quantity(10000)
//                      .averagePrice(1.12)
//                      .currentPrice(1.10)
//                      .value(11200.0)
//                      .gainLoss(200.0)
//                      .build();

//         when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(oldAsset));
//         when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);

//         Asset updatedAsset = assetService.processSellTrade(testTrade);

//         assertNull(updatedAsset);
//         verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
//         verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
//     }

//     @Test
//     public void processSellTrade_NewQuantityNotZero_NeedToUpdateCurrentPrice_ReturnAsset() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         List<Asset> testAssetList = new ArrayList<>();
        
//         // Mock portfolio
//         Portfolio testPortfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .assets(testAssetList)
//                                 .totalGainLoss(-250.0)
//                                 .unrealizedGainLoss(0.0)
//                                 .build();
        
//         Date date = new Date(1602321010000L);
//         Stock testStock = new Stock("A1");

//         //Mock stockRecord to return
//         StockRecord testStockRecord = StockRecord.builder()
//                                       .stock(testStock)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();
        
//         // Mock trade
//         Trade testTrade = Trade.builder()
//                           .stock(testStock).action(Action.SELL)
//                           .quantity(50000).filledQuantity(5000)
//                           .customerId(testCustomerId).accountId(1)
//                           .submittedDate(date)
//                           .status(Status.PARTIAL_FILLED).price(1.18).build();
        
//         // Mock the asset record which user currently owns
//         Asset testAsset = Asset.builder()
//                      .portfolio(testPortfolio)
//                      .code("A1")
//                      .quantity(10000)
//                      .averagePrice(1.12)
//                      .currentPrice(1.10)
//                      .value(11200.0)
//                      .gainLoss(-200.0)
//                      .build();
        
//         testAssetList.add(testAsset);

//         // // New asset record after user completes sell trade
//         // Asset newAsset = Asset.builder()
//         //                  .portfolio(testPortfolio)
//         //                  .code("A1")
//         //                  .quantity(5000)
//         //                  .averagePrice(1.12)
//         //                  .currentPrice(1.11)
//         //                  .value(5600.0)
//         //                  .gainLoss(-50.0)
//         //                  .build();

//         when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol())).thenReturn(Optional.of(testAsset));
//         when(stockRecordService.getLatestStockRecordBySymbol(testStock.getSymbol())).thenReturn(testStockRecord);

//         Asset returnedAsset = assetService.processSellTrade(testTrade);

//         assertEquals(5000, returnedAsset.getQuantity());
//         assertEquals(1.12, returnedAsset.getAveragePrice());
//         assertEquals(1.11, returnedAsset.getCurrentPrice());
//         assertEquals(5600.0, returnedAsset.getValue());
//         assertEquals(-50.0, returnedAsset.getGainLoss());
//         verify(assets).findByPortfolioCustomerIdAndCode(testCustomerId, testStock.getSymbol());
//         verify(assets).save(returnedAsset);
//         verify(stockRecordService).getLatestStockRecordBySymbol(testStock.getSymbol());
//     }

//     @Test 
//     public void updateAssets_ChangeCurrentPriceAndGainLoss() {
        
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         List<Asset> testAssetList = new ArrayList<>();
        
//         // Mock portfolio
//         Portfolio testPortfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .totalGainLoss(0.0)
//                                 .unrealizedGainLoss(-200.0)
//                                 .build();

//         // Mock the asset record which user currently owns
//         Asset testAsset1 = Asset.builder()
//                          .portfolio(testPortfolio)
//                          .code("TEST1")
//                          .quantity(10000)
//                          .averagePrice(1.12)
//                          .currentPrice(1.10)
//                          .value(11200.0)
//                          .gainLoss(-200.0)
//                          .build();
        
//         testAssetList.add(testAsset1);

//         Date date = new Date(1602321010000L);
//         Stock testStock1 = new Stock("TEST1");
//         Stock testStock2 = new Stock("TEST2");

//         //Mock stockRecord to return
//         StockRecord testStockRecord1 = StockRecord.builder()
//                                       .stock(testStock1)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();

//         // Asset updatedAsset1 = Asset.builder()
//         //                  .portfolio(testPortfolio)
//         //                  .code("TEST1")
//         //                  .quantity(10000)
//         //                  .averagePrice(1.12)
//         //                  .currentPrice(1.11)
//         //                  .value(11200.0)
//         //                  .gainLoss(-100.0)
//         //                  .build();
        
//         when(assets.findByPortfolioCustomerId(testCustomerId)).thenReturn(testAssetList);
//         when(stockRecordService.getLatestStockRecordBySymbol(testStock1.getSymbol())).thenReturn(testStockRecord1);

//         List<Asset> returnedAssetList = assetService.updateAssets(testPortfolio);
//         Asset returnedAsset = returnedAssetList.get(0);
//         assertEquals(1, returnedAssetList.size());
//         assertEquals("TEST1", returnedAsset.getCode());
//         assertEquals(10000, returnedAsset.getQuantity());
//         assertEquals(1.12, returnedAsset.getAveragePrice());
//         assertEquals(1.11, returnedAsset.getCurrentPrice());
//         assertEquals(11200.0, returnedAsset.getValue());
//         assertEquals(-100.0, returnedAsset.getGainLoss());

//         verify(assets).findByPortfolioCustomerId(testCustomerId);
//         verify(stockRecordService).getLatestStockRecordBySymbol(testStock1.getSymbol());
//         verify(assets).save(returnedAsset);
//     }
// }