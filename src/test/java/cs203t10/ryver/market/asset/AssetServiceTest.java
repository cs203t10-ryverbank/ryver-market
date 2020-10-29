// package cs203t10.ryver.market.asset;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

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
// import cs203t10.ryver.market.portfolio.PortfolioService;
// import cs203t10.ryver.market.portfolio.PortfolioServiceImpl;
// import cs203t10.ryver.market.portfolio.asset.Asset;
// import cs203t10.ryver.market.portfolio.asset.AssetRepository;
// import cs203t10.ryver.market.portfolio.asset.AssetServiceImpl;

// @ExtendWith(MockitoExtension.class)
// public class AssetServiceTest {

//     @Mock
//     private AssetRepository assets;

//     @Mock
//     private PortfolioService portfolioService;

//     @InjectMocks
//     private AssetServiceImpl assetService;

//     @Test
//     public void deductFromAsset_Valid_QuantityNotZero_ReturnsAsset() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
//         List<Asset> testAssetList = new ArrayList<>();

//         Portfolio testPortfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .assets(testAssetList)
//                                 .totalGainLoss(0.0)
//                                 .build();

//         Asset testAsset = Asset.builder()
//                           .code("TEST")
//                           .portfolio(testPortfolio)
//                           .quantity(1000)
//                           .averagePrice(1.11)
//                           .value(1110.0)
//                           .build();
//         testAssetList.add(testAsset);

//         Asset expectedAsset = Asset.builder()
//                               .id(testAsset.getId())
//                               .code("TEST")
//                               .portfolio(testPortfolio)
//                               .quantity(500)
//                               .averagePrice(1.11)
//                               .value(555.0)
//                               .build();

//         when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testAsset.getCode())).thenReturn(Optional.of(testAsset));
//         when(assets.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

//         Asset returnedAsset = assetService.deductFromAsset(testCustomerId, testAsset.getCode(), 500);
//         assertEquals(expectedAsset, returnedAsset);
//         verify(assets).save(expectedAsset);
//     }

//     @Test
//     public void addToAsset_Valid_StockOwned_ReturnsAsset() {
//         Random rand = new Random();
//         Integer testCustomerId = rand.nextInt(Integer.MAX_VALUE);
//         List<Asset> testAssetList = new ArrayList<>();

//         Portfolio testPortfolio = Portfolio.builder()
//                                 .customerId(testCustomerId)
//                                 .assets(testAssetList)
//                                 .totalGainLoss(0.0)
//                                 .build();

//         Asset testAsset = Asset.builder()
//                           .code("TEST")
//                           .portfolio(testPortfolio)
//                           .quantity(1000)
//                           .averagePrice(1.11)
//                           .value(1110.0)
//                           .build();
//         testAssetList.add(testAsset);

//         Asset expectedAsset = Asset.builder()
//                               .id(testAsset.getId())
//                               .code("TEST")
//                               .portfolio(testPortfolio)
//                               .quantity(1500)
//                               .averagePrice(1.47)
//                               .value(2210.0)
//                               .build();

//         when(assets.findByPortfolioCustomerIdAndCode(testCustomerId, testAsset.getCode())).thenReturn(Optional.of(testAsset));
//         when(assets.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

//         Asset returnedAsset = assetService.addToAsset(testCustomerId, testAsset.getCode(), 500, 2.20);
//         assertEquals(expectedAsset, returnedAsset);
//         verify(assets).save(expectedAsset);
//     }
// }