// package cs203t10.ryver.market.portfolio;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.sql.Date;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.Optional;
// import java.util.Random;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import cs203t10.ryver.market.portfolio.Portfolio;
// import cs203t10.ryver.market.portfolio.PortfolioRepository;
// import cs203t10.ryver.market.portfolio.PortfolioServiceImpl;
// import cs203t10.ryver.market.portfolio.asset.Asset;
// import cs203t10.ryver.market.portfolio.asset.AssetRepository;
// import cs203t10.ryver.market.portfolio.asset.AssetServiceImpl;
// import cs203t10.ryver.market.portfolio.asset.view.AssetInfoViewableByCustomer;
// import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;
// import cs203t10.ryver.market.stock.Stock;
// import cs203t10.ryver.market.stock.StockRecord;
// import cs203t10.ryver.market.stock.StockRecordRepository;
// import cs203t10.ryver.market.stock.StockRecordServiceImpl;
// import cs203t10.ryver.market.trade.Trade;
// import cs203t10.ryver.market.trade.Trade.Action;
// import cs203t10.ryver.market.trade.Trade.Status;

// @ExtendWith(MockitoExtension.class)
// public class PortfolioServiceTest {

//     @Mock
//     private PortfolioRepository portfolios;

//     @Mock
//     private StockRecordServiceImpl stockRecordService;

//     @Mock
//     private AssetServiceImpl assetService;

//     @Mock
//     private AssetRepository assets;

//     @InjectMocks
//     private PortfolioServiceImpl portfolioService;

//     @Test
//     public void createPortfolio_NewPortfolio_ReturnsPortfolio() {
        
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
        
//         Portfolio testPortfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .totalGainLoss(0.0)
//                                 .unrealizedGainLoss(0.0)
//                                 .build();
        
//         when(portfolios.save(testPortfolio)).thenReturn(testPortfolio);

//         Portfolio createdPortfolio = portfolioService.createPortfolio(testCustomerId);

//         assertEquals(testPortfolio, createdPortfolio);
//         verify(portfolios).save(testPortfolio);
//     }

//     @Test
//     public void viewPortfolio_NoPortfolio_ReturnsPortfolioInfoViewableByCustomer() {
        
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         Portfolio testPortfolio = Portfolio.builder()
//                               .customerId(testCustomerId)
//                               .totalGainLoss(0.0)
//                               .unrealizedGainLoss(0.0)
//                               .build();

//         List<AssetInfoViewableByCustomer> testAssetInfoList = new ArrayList<>();
        
//         PortfolioInfoViewableByCustomer testPortfolioInfo = new PortfolioInfoViewableByCustomer(
//             testPortfolio.getCustomerId(),
//             testAssetInfoList,
//             testPortfolio.getUnrealizedGainLoss(),
//             testPortfolio.getTotalGainLoss());
        
//         when(portfolios.save(testPortfolio)).thenReturn(testPortfolio);
//         when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.empty());

//         PortfolioInfoViewableByCustomer portfolioInfo = portfolioService.viewPortfolio(testCustomerId);
        
//         assertEquals(testPortfolioInfo, portfolioInfo);
//         verify(portfolios).save(testPortfolio); 
//         verify(portfolios).findByCustomerId(testCustomerId);
//     }

//     @Test
//     public void calculateUnrealizedGainLoss() {

//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
//         List<Asset> assetList = new ArrayList<>();

//         // Mock portfolio
//         Portfolio portfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .assets(assetList)
//                                 .totalGainLoss(0.0)
//                                 .unrealizedGainLoss(0.0)
//                                 .build();


//         Asset testAsset1 = Asset.builder()
//                            .portfolio(portfolio)
//                            .code("A1")
//                            .quantity(10000)
//                            .averagePrice(1.12)
//                            .currentPrice(1.10)
//                            .value(11200.0)
//                            .gainLoss(200.0)
//                            .build();
        
//         Asset testAsset2 = Asset.builder()
//                            .portfolio(portfolio)
//                            .code("A2")
//                            .quantity(10000)
//                            .averagePrice(1.12)
//                            .currentPrice(1.10)
//                            .value(11200.0)
//                            .gainLoss(200.0)
//                            .build();
        
//         assetList.add(testAsset1);
//         assetList.add(testAsset2);
        
//         Double calculatedUnrealizedGainLoss = portfolioService.calculateUnrealizedGainLoss(portfolio);

//         assertEquals(400.0, calculatedUnrealizedGainLoss);
//     }

//     @Test
//     public void processSellTrade_StockOwned_UpdateUnrealizedGainLoss() {
        
//         Stock testStock = new Stock("TEST1");

//         Date date = new Date(1602321010000L);
//         StockRecord testStockRecord = StockRecord.builder()
//                                       .stock(testStock)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();
        
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
//         List<Asset> testAssetList = new ArrayList<>();
//         Portfolio testPortfolio = Portfolio.builder()
//                                  .customerId(testCustomerId)
//                                  .assets(testAssetList)
//                                  .totalGainLoss(0.0)
//                                  .unrealizedGainLoss(-400.0)
//                                  .build();
//         Asset testAsset1 = Asset.builder()
//                            .portfolio(testPortfolio)
//                            .code("TEST1")
//                            .quantity(10000)
//                            .averagePrice(1.12)
//                            .currentPrice(1.10)
//                            .value(11200.0)
//                            .gainLoss(-200.0)
//                            .build();
//         Asset testAsset2 = Asset.builder()
//                            .portfolio(testPortfolio)
//                            .code("TEST2")
//                            .quantity(10000)
//                            .averagePrice(1.12)
//                            .currentPrice(1.10)
//                            .value(11200.0)
//                            .gainLoss(-200.0)
//                            .build();
//         testAssetList.add(testAsset1);
//         testAssetList.add(testAsset2);

//         Trade testTrade = Trade.builder()
//                           .stock(testStock).action(Action.SELL)
//                           .quantity(50000).filledQuantity(5000)
//                           .customerId(testCustomerId).accountId(1)
//                           .submittedDate(date)
//                           .status(Status.PARTIAL_FILLED).price(1.18).build();

//         when(assetService.findByPortfolioCustomerIdAndCode(testCustomerId, "TEST1")).thenReturn(testAsset1);
//         when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.of(testPortfolio));
//         when(portfolios.save(testPortfolio)).thenReturn(testPortfolio);
//         Portfolio returnedPortfolio = portfolioService.processBuyTrade(testTrade);
//         assertEquals(350.0, returnedPortfolio.getTotalGainLoss());
//     }                
// }
