package cs203t10.ryver.market.exception;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.apache.commons.lang3.StringUtils;
 
@SuppressWarnings({"unchecked","rawtypes"})
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        CustomResponse error = new CustomResponse(Instant.now(), 
                                                status, 
                                                StringUtils.substringBefore(ex.getMessage(), ":"), 
                                                StringUtils.substringBefore(ex.getCause().getLocalizedMessage(), ":"),
                                                request.getDescription(false).replace("uri=", ""));
        ResponseEntity<Object> response = new ResponseEntity(error, status);
        return response;
    }

}