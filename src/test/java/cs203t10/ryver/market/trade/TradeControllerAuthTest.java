package cs203t10.ryver.market.trade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import cs203t10.ryver.market.security.RyverPrincipal;
import cs203t10.ryver.market.security.RyverPrincipalInjector;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerAuthTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TradeController tradeController;

    RyverPrincipal managerPrincipal = RyverPrincipal.builder()
            .uid(1L).username("manager_1").build();

    RyverPrincipal userPrincipal = RyverPrincipal.builder()
            .uid(3L).username("marktan").build();
    RyverPrincipalInjector userPrincipalInjector = new RyverPrincipalInjector() {
        public RyverPrincipal getRyverPrincipal() {
            return userPrincipal;
        }
    };

    @BeforeEach
    public void mockMvcSetup() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(tradeController)
            .setCustomArgumentResolvers(userPrincipalInjector)
            .build();
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void getTradesAsUser() throws Exception {
        mockMvc.perform(get("/trades"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    public void getTradesAsManager() {
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            try {
                mockMvc.perform(get("/trades"));
            } catch (NestedServletException e) {
                throw e.getRootCause();
            }
        });
    }

    @Test
    public void getTradesAnonymous() {
        Assertions.assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            try {
                mockMvc.perform(get("/trades"));
            } catch (NestedServletException e) {
                throw e.getRootCause();
            }
        });
    }

}

