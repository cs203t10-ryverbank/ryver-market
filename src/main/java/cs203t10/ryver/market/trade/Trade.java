package cs203t10.ryver.market.trade;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Data @Builder(toBuilder = true)
@EqualsAndHashCode @ToString
public class Trade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(regexp = "buy|sell", message = "Not a valid action")
    private String action;

    @Pattern(regexp = "A17U|C61U|C31|C38U|C09|C52|D01|D05|G13|H78|C07|J36|J37|BN4|N2IU|ME8U|M44U|O39|S58|U96|S68|C6L|Z74|S63|Y92|U11|U14|V03|F34|BS6",
            message = "Not a valid symbol")
    private String symbol;

    private Integer quantity;

    private Double bid;

    private Double ask;

    private Double avgPrice;

    private Integer filledQuantity;

    private Date submittedDate;

    private Integer accountId;

    private Integer customerId;

    @Pattern(regexp = "open|filled|partial-filled|cancelled|expired")
    private String status;

}

