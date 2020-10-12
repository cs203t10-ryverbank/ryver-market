package cs203t10.ryver.market.trade;

import java.util.List;

public interface ExtendedTradeRepository {
    List<Trade> findAllByCustomerId(Long customerId);
}
