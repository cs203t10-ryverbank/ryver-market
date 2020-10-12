package cs203t10.ryver.market.trade;

import java.util.List;

public interface TradeService {
    Trade saveTrade (Trade trade);
    Trade getTrade (Integer tradeId);
    List<Trade> getAllUserOpenTrades(Long customerId);
}
