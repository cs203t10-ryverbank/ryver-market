package cs203t10.ryver.market.stock.scrape;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.*;

@Entity
@Data
public class SgxScrapingMetadata {
    @Id
    private String key;

    private String value;
}
