package cs203t10.ryver.market.stock.scrape;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Store metadata about web scraping such as previous scraping date.
 */
@Repository
public interface SgxScrapingMetadataRepository extends JpaRepository<SgxScrapingMetadata, String> {

}

