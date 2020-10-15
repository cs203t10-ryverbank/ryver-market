package cs203t10.ryver.market.trade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.NestedServletException;

import cs203t10.ryver.market.security.RyverPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerAuthTest {

    @Autowired
    MockMvc mockMvc;

    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(RyverPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            return RyverPrincipal.builder().uid(3L).username("marktan").build();
        }
    };

    @Autowired
    TradeController tradeController;

    RyverPrincipal managerPrincipal = RyverPrincipal.builder()
            .uid(1L).username("manager_1").build();
    RyverPrincipal userPrincipal = RyverPrincipal.builder()
            .uid(3L).username("marktan").build();

    @BeforeEach
    public void mockMvcSetup() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(tradeController)
            .setCustomArgumentResolvers(putAuthenticationPrincipal)
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

