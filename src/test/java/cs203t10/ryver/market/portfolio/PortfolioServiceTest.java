// package cs203t10.ryver.market.portfolio;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.any;
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
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;

// import cs203t10.ryver.market.fund.FundTransferService;
// import cs203t10.ryver.market.portfolio.asset.Asset;
// import cs203t10.ryver.market.portfolio.asset.AssetService;
// import cs203t10.ryver.market.portfolio.asset.view.AssetInfoViewableByCustomer;
// import cs203t10.ryver.market.portfolio.view.PortfolioInfoViewableByCustomer;
// import cs203t10.ryver.market.stock.Stock;
// import cs203t10.ryver.market.stock.StockRecord;
// import cs203t10.ryver.market.stock.StockRecordService;
// import cs203t10.ryver.market.trade.Trade;
// import cs203t10.ryver.market.trade.Trade.Action;
// import cs203t10.ryver.market.trade.Trade.Status;

// @ExtendWith(MockitoExtension.class)
// public class PortfolioServiceTest {

//     @Mock
//     private PortfolioRepository portfolios;

//     @Mock
//     private AssetService assetService;

//     @Mock
//     private StockRecordService stockRecordService;

//     @Mock
//     private FundTransferService fundTransferService;

//     @InjectMocks
//     private PortfolioServiceImpl portfolioService;

//     // @Test
//     // public void viewPortfolio_PortfolioDoesNotExist_CreatePortfolio_ReturnPortfolioInfo() {
//     //     Random rand = new Random();
//     //     Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//     //     List<Asset> testAssetList = new ArrayList<>();
//     //     PortfolioInitial portfolioInitial = new PortfolioInitial(testCustomerId, testAssetList, 1000.0);

//     //     List<AssetInfoViewableByCustomer> expectedAssetInfoList = new ArrayList<>();
//     //     PortfolioInfoViewableByCustomer expectedPortfolioInfo = new PortfolioInfoViewableByCustomer(testCustomerId, expectedAssetInfoList, 0.0, 0.0);

//     //     HttpHeaders header = new HttpHeaders();
//     //     header.setContentType(MediaType.APPLICATION_JSON);
//     //     ResponseEntity<String> ftsResponseEntity = new ResponseEntity<>("1000.0", header, HttpStatus.OK);

//     //     when(portfolios.save(any(Portfolio.class))).thenAnswer(i -> i.getArguments()[0]);
//     //     when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.empty());
//     //     when(assetService.findByPortfolioCustomerId(testCustomerId)).thenReturn(testAssetList);
//     //     when(fundTransferService.getTotalBalance(testCustomerId)).thenReturn(ftsResponseEntity);

//     //     PortfolioInfoViewableByCustomer returnedPortfolioInfo = portfolioService.viewPortfolio(testCustomerId);

//     //     assertEquals(expectedPortfolioInfo, returnedPortfolioInfo);
//     //     verify(portfolios).save(portfolioInitial.toPortfolio());
//     // }

//     @Test
//     public void viewPortfolio_PortfolioExists_ReturnPortfolioInfo() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         List<Asset> testAssetList = new ArrayList<>();

//         Portfolio testPortfolio = Portfolio.builder()
//                                       .customerId(testCustomerId)
//                                       .assets(testAssetList)
//                                       .initialCapital(1000.0)
//                                       .build();

//         Asset testAsset1 = Asset.builder()
//                            .code("TEST1")
//                            .portfolio(testPortfolio)
//                            .quantity(1000)
//                            .averagePrice(1.11)
//                            .value(1110.0)
//                            .build();
//         Asset testAsset2 = Asset.builder()
//                            .code("TEST2")
//                            .portfolio(testPortfolio)
//                            .quantity(2000)
//                            .averagePrice(1.10)
//                            .value(2200.0)
//                            .build();
//         testAssetList.add(testAsset1);
//         testAssetList.add(testAsset2);

//         Stock testStock1 = new Stock("TEST1");
//         Stock testStock2 = new Stock("TEST1");

//         Date date = new Date(1602321010000L);
//         StockRecord testStockRecord1 = StockRecord.builder()
//                                       .stock(testStock1)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();
//         StockRecord testStockRecord2 = StockRecord.builder()
//                                       .stock(testStock2)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();

//         List<AssetInfoViewableByCustomer> expectedAssetInfoList = new ArrayList<>();
//         AssetInfoViewableByCustomer expectedAssetInfo1 = new AssetInfoViewableByCustomer("TEST1", 1000, 1.11, 1.11, 1110.0, 0.0);
//         AssetInfoViewableByCustomer expectedAssetInfo2 = new AssetInfoViewableByCustomer("TEST2", 2000, 1.10, 1.11, 2200.0, 20.0);
//         expectedAssetInfoList.add(expectedAssetInfo1);
//         expectedAssetInfoList.add(expectedAssetInfo2);

//         PortfolioInfoViewableByCustomer expectedPortfolioInfo = new PortfolioInfoViewableByCustomer(testCustomerId, expectedAssetInfoList, 20.0, 1000.0);

//         HttpHeaders header = new HttpHeaders();
//         header.setContentType(MediaType.APPLICATION_JSON);
//         ResponseEntity<String> ftsResponseEntity = new ResponseEntity<>("2000.0", header, HttpStatus.OK);

//         when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.of(testPortfolio));
//         when(assetService.findByPortfolioCustomerId(testCustomerId)).thenReturn(testAssetList);
//         when(stockRecordService.getLatestStockRecordBySymbol("TEST1")).thenReturn(testStockRecord1);
//         when(stockRecordService.getLatestStockRecordBySymbol("TEST2")).thenReturn(testStockRecord2);
//         when(fundTransferService.getTotalBalance(testCustomerId)).thenReturn(ftsResponseEntity);
//         PortfolioInfoViewableByCustomer returnedPortfolioInfo = portfolioService.viewPortfolio(testCustomerId);

//         assertEquals(expectedPortfolioInfo, returnedPortfolioInfo);
//     }

//     @Test
//     public void processSellTrade_Valid_UpdateTotalGainLoss_ReturnPortfolio() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);

//         List<Asset> testAssetList = new ArrayList<>();

//         Portfolio testPortfolio = Portfolio.builder()
//                                       .customerId(testCustomerId)
//                                       .assets(testAssetList)
//                                       .initialCapital(1000.0)
//                                       .build();

//         Asset testAsset1 = Asset.builder()
//                            .code("TEST1")
//                            .portfolio(testPortfolio)
//                            .quantity(1000)
//                            .averagePrice(1.11)
//                            .value(1110.0)
//                            .build();
//         Asset testAsset2 = Asset.builder()
//                            .code("TEST2")
//                            .portfolio(testPortfolio)
//                            .quantity(2000)
//                            .averagePrice(1.10)
//                            .value(2200.0)
//                            .build();
//         testAssetList.add(testAsset1);
//         testAssetList.add(testAsset2);

//         Stock testStock1 = new Stock("TEST1");

//         Date date = new Date(1602321010000L);
//         StockRecord testStockRecord1 = StockRecord.builder()
//                                       .stock(testStock1)
//                                       .submittedDate(date)
//                                       .price(1.11)
//                                       .totalVolume(1000000)
//                                       .build();

//         Trade testTrade = Trade.builder()
//                           .stock(testStock1).action(Action.SELL)
//                           .quantity(50000).filledQuantity(500)
//                           .customerId(testCustomerId).accountId(1)
//                           .submittedDate(date)
//                           .status(Status.PARTIAL_FILLED).price(1.18).build();

//         List<Asset> updatedAssetList = new ArrayList<>();

//         Portfolio expectedPortfolio = Portfolio.builder()
//                                       .customerId(testCustomerId)
//                                       .assets(updatedAssetList)
//                                       .initialCapital(1000.0)
//                                       .build();

//         Asset updatedAsset1 = Asset.builder()
//                            .code("TEST1")
//                            .portfolio(testPortfolio)
//                            .quantity(500)
//                            .averagePrice(1.11)
//                            .value(555.0)
//                            .build();
//         updatedAssetList.add(updatedAsset1);
//         updatedAssetList.add(testAsset2);

//         when(portfolios.findByCustomerId(testCustomerId)).thenReturn(Optional.of(testPortfolio));
//         when(stockRecordService.getLatestStockRecordBySymbol("TEST1")).thenReturn(testStockRecord1);
//         when(portfolios.save(any(Portfolio.class))).thenAnswer(i -> i.getArguments()[0]);

//         Portfolio returnedPortfolio = portfolioService.processSellTrade(testTrade);

//         assertEquals(expectedPortfolio, returnedPortfolio);
//         verify(assetService).deductFromAsset(testCustomerId, "TEST1", 500);
//     }
// }
