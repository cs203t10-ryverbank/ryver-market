package cs203t10.ryver.market.trade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer>, ExtendedTradeRepository {

}
