package cs203t10.ryver.market.trade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExtendedTradeRepository {
    Optional<Trade> findLatestBySymbol(String symbol);
    List<Trade> findAllByCustomerId(Long customerId);
    Trade saveWithSymbol(Trade trade, String symbol);
    List<Trade> findAllLatestPerStock();
    Long getTotalQuantityBySymbol(String symbol);
    Long getBuyQuantityBySymbol(String symbol);
    Long getSellQuantityBySymbol(String symbol);
    Optional<Trade> findBestBuyBySymbol(String symbol);
    Optional<Trade> findBestSellBySymbol(String symbol);
    Map<String, Trade> findAllBestBuy();
    Map<String, Trade> findAllBestSell();
}
