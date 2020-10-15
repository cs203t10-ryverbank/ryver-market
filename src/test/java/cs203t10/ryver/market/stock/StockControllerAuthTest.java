package cs203t10.ryver.market.stock;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class StockControllerAuthTest {

    @Mock
    StockRecordService stockRecordService;

    @Mock
    TradeService tradeService;

    @InjectMocks
    StockController stockController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    FilterChainProxy springSecurityFilterChain;

    @Test
    @WithMockUser(roles = { "USER" })
    public void getStocksAsUser_isOk() throws Exception {
        mockMvc.perform(get("/stocks")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void getStocksAsManager_isForbidden() throws Exception {
        mockMvc.perform(get("/stocks")).andExpect(status().isForbidden());
    }

    @Test
    public void getStocksAnonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get("/stocks")).andExpect(status().isUnauthorized());
    }

    Date firstDate = new Date(1602321010000L);
    Date secondDate = new Date(1602324610000L);

    Stock testStock = new Stock("TEST");
    StockRecord testRecord = StockRecord.builder()
            .stock(testStock).submittedDate(firstDate)
            .price(1.2).totalVolume(34500).build();
    Trade testBuy = Trade.builder()
            .stock(testStock).action(Action.BUY)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(secondDate)
            .status(Status.OPEN).price(1.18).build();
    Trade testSell = Trade.builder()
            .stock(testStock).action(Action.SELL)
            .quantity(10000).filledQuantity(0)
            .customerId(1).accountId(1)
            .submittedDate(secondDate)
            .status(Status.OPEN).price(1.23).build();

    @Test
    @WithMockUser(roles = { "USER" })
    public void getStockAsUser_isOk() throws Exception {
        // Set up mocked services
        when(stockRecordService.getLatestStockRecordBySymbol("TEST"))
            .thenReturn(testRecord);
        when(tradeService.getBestBuy("TEST"))
            .thenReturn(testBuy);
        when(tradeService.getBestBuy("TEST"))
            .thenReturn(testSell);

        // Set up mockMvc
        mockMvc = MockMvcBuilders
            .standaloneSetup(stockController)
            .build();

        mockMvc.perform(get("/stocks/TEST")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void getStockAsManager_isForbidden() throws Exception {
        mockMvc.perform(get("/stocks/TEST")).andExpect(status().isForbidden());
    }

    @Test
    public void getStockAnonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get("/stocks/TEST")).andExpect(status().isUnauthorized());
    }

}

