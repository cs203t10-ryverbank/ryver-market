package cs203t10.ryver.market.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public final class PrincipalService {

    public RyverPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Current context is not authenticated");
        }

        Object principalObj = auth.getPrincipal();
        if (principalObj == null) {
            throw new RuntimeException("Principal is null");
        }
        if (!(principalObj instanceof RyverPrincipal)) {
            throw new RuntimeException("Principal is not instanceof RyverPrincipal");
        }
        return (RyverPrincipal) principalObj;
    }

}
