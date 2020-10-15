package cs203t10.ryver.market.trade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import cs203t10.ryver.market.security.RyverPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerAuthTest {

    @Autowired
    MockMvc mockMvc;

    RyverPrincipal managerPrincipal = RyverPrincipal.builder()
            .uid(1L).username("manager_1").build();
    RyverPrincipal userPrincipal = RyverPrincipal.builder()
            .uid(3L).username("marktan").build();

    @Test
    @WithMockUser(roles = { "USER" })
    public void getTradesAsUser() throws Exception {
        mockMvc.perform(get("/trades"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void getTradesAsManager() throws Exception {
        mockMvc.perform(get("/trades"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void getTradesAnonymous() throws Exception {
        mockMvc.perform(get("/trades"))
            .andExpect(status().isUnauthorized());
    }

}

