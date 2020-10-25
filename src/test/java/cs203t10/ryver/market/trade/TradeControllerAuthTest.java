package cs203t10.ryver.market.trade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.security.PrincipalService;
import cs203t10.ryver.market.security.RyverPrincipal;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class TradeControllerAuthTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PrincipalService principalService;

    @Autowired
    TradeService tradeService;

    RyverPrincipal managerPrincipal = RyverPrincipal.builder()
            .uid(1L).username("manager_1").build();

    RyverPrincipal userPrincipal = RyverPrincipal.builder()
            .uid(3L).username("marktan").build();

    @Test
    @WithMockUser(roles = { "USER" })
    public void getTradesAsUser() throws Exception {
        // Set up principal service
        when(principalService.getPrincipal())
            .thenReturn(userPrincipal);

        mockMvc.perform(get("/trades"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void getTradesAsManager() throws Exception {
        // Set up principal service
        when(principalService.getPrincipal())
            .thenReturn(managerPrincipal);

        mockMvc.perform(get("/trades"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void getTradesAnonymous() throws Exception {
        // Set up principal service
        when(principalService.getPrincipal())
            .thenReturn(managerPrincipal);

        mockMvc.perform(get("/trades"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void getTradeAsOtherCustomer() throws Exception {
        Date testDate = new Date(1602321010000L);

        Stock testStock = new Stock("TEST");
        Trade testTrade = Trade.builder()
                .stock(testStock).action(Action.BUY)
                .quantity(1000).filledQuantity(0)
                .customerId(2).accountId(1)
                .submittedDate(testDate)
                .status(Status.OPEN).price(1.18)
                .build();

        when(principalService.getPrincipal())
            .thenReturn(userPrincipal);
        when(tradeService.getTrade(any(Integer.class)))
            .thenReturn(testTrade);

        mockMvc.perform(get("/trades/1"))
            .andExpect(status().isForbidden());
        verify(tradeService).getTrade(any(Integer.class));
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void addTradeAsOtherCustomer() throws Exception {
        Date testDate = new Date(1602321010000L);

        TradeView testTradeView = TradeView.builder()
                .action(Action.BUY).symbol("TEST")
                .quantity(1000).bid(3.28)
                .ask(3.27).avgPrice(3.30)
                .filledQuantity(0).submittedDate(testDate)
                .accountId(1).customerId(2)
                .status(Status.OPEN)
                .build();

        when(principalService.getPrincipal())
            .thenReturn(userPrincipal);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        String postBody = objectMapper.writeValueAsString(testTradeView);
        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                .andExpect(status().isForbidden());
        
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void addTradeAsManager() throws Exception {
        Date testDate = new Date(1602321010000L);

        TradeView testTradeView = TradeView.builder()
                .action(Action.BUY).symbol("TEST")
                .quantity(1000).bid(3.28)
                .ask(3.27).avgPrice(3.30)
                .filledQuantity(0).submittedDate(testDate)
                .accountId(1).customerId(1)
                .status(Status.OPEN)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        String postBody = objectMapper.writeValueAsString(testTradeView);
        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void deleteTradeAsOtherCustomer() throws Exception {
        Date testDate = new Date(1602321010000L);

        Stock testStock = new Stock("TEST");
        Trade testTrade = Trade.builder()
                .stock(testStock).action(Action.BUY)
                .quantity(1000).filledQuantity(0)
                .customerId(2).accountId(1)
                .submittedDate(testDate)
                .status(Status.OPEN).price(1.18)
                .build();

        when(principalService.getPrincipal())
            .thenReturn(userPrincipal);
        when(tradeService.getTrade(any(Integer.class)))
            .thenReturn(testTrade);
        
        mockMvc.perform(delete("/trades/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void deleteNonexistentTrade() throws Exception {
        when(principalService.getPrincipal())
            .thenReturn(userPrincipal);
        when(tradeService.getTrade(any(Integer.class)))
            .thenReturn(null);
        mockMvc.perform(delete("/trades/1"))
                .andExpect(status().isNotFound());
    }

}

