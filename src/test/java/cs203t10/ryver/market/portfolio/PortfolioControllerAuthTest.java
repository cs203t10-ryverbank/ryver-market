package cs203t10.ryver.market.portfolio;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import cs203t10.ryver.market.security.PrincipalService;
import cs203t10.ryver.market.security.RyverPrincipal;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class PortfolioControllerAuthTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    PrincipalService principalService;

    @Autowired
    PortfolioRepository portfolios;

    RyverPrincipal userPrincipal = RyverPrincipal.builder()
            .uid(3L).username("marktan").build();

    @Test
    @WithMockUser(roles = { "USER" })
    public void getPortfolioAsUser_isOk() throws Exception {
        // when(principalService.getPrincipal())
        //     .thenReturn(userPrincipal);
        mockMvc.perform(get("/portfolio")).andExpect(status().isOk());
    }
    
}
