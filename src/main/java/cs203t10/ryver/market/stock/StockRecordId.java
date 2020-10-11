package cs203t10.ryver.market.stock;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.*;

@Data
@NoArgsConstructor @AllArgsConstructor
public class StockRecordId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Stock stock;
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedDate;

}

