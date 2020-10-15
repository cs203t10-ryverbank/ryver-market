package cs203t10.ryver.market.trade;

import java.util.List;
import java.util.Optional;

public interface ExtendedTradeRepository {
    Trade saveWithSymbol(Trade trade, String symbol);
    Optional<Trade> findLatestBySymbol(String symbol);
    List<Trade> findAllByCustomerId(Long customerId);
    List<Trade> findAllLatestPerStock();
    Optional<Trade> findBestMarketBuyBySymbol(String symbol);
    Optional<Trade> findBestMarketSellBySymbol(String symbol);
    Optional<Trade> findBestLimitBuyBySymbol(String symbol);
    Optional<Trade> findBestLimitSellBySymbol(String symbol);
    Long getTotalQuantityBySymbol(String symbol);
    Long getBuyQuantityBySymbol(String symbol);
    Long getSellQuantityBySymbol(String symbol);
}
