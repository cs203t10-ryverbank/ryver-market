package cs203t10.ryver.market.stock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class StockControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = { "USER" })
    public void getStocksAsUser() throws Exception {
        mockMvc.perform(get("/stocks")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void getStocksAsManager() throws Exception {
        mockMvc.perform(get("/stocks")).andExpect(status().isForbidden());
    }

    @Test
    public void getStocksAnonymous() throws Exception {
        mockMvc.perform(get("/stocks")).andExpect(status().isUnauthorized());
    }

}

