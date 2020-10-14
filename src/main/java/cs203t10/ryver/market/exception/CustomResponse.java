package cs203t10.ryver.market.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import lombok.*;
 
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CustomResponse extends Object {
    private Instant timestamp;

    private HttpStatus status;

    private String error;

    private String message;
 
    private String path;
 
}
