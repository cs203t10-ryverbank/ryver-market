package cs203t10.ryver.market.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@FunctionalInterface
public interface RyverPrincipalInjector extends HandlerMethodArgumentResolver {

    @Override
    default boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(RyverPrincipal.class);
    }

    @Override
    default Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return getRyverPrincipal();
    }

    RyverPrincipal getRyverPrincipal();

}

