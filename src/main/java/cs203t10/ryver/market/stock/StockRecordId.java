package cs203t10.ryver.market.stock;

import java.io.Serializable;
import java.util.Date;

import lombok.*;

@AllArgsConstructor
@EqualsAndHashCode
public class StockRecordId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Stock stock;

    private Date submittedDate;

}

