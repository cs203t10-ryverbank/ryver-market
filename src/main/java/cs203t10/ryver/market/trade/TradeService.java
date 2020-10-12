package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.trade.view.TradeView;

public interface TradeService {
    Trade saveTrade(TradeView tradeView);
    Trade getTrade(Integer tradeId);
    List<Trade> getAllUserOpenTrades(Long customerId);
}
