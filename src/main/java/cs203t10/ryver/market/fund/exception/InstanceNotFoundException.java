package cs203t10.ryver.market.fund.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InstanceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InstanceNotFoundException(String serviceName) {
        super(String.format("No instance of service %s found", serviceName));
    }

}

