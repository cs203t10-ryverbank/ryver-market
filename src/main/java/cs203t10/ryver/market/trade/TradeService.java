package cs203t10.ryver.market.trade;

import java.util.List;
import java.util.Map;

import cs203t10.ryver.market.trade.view.TradeView;

public interface TradeService {
    Trade saveTrade(TradeView tradeView);
    Trade getTrade(Integer tradeId);
    Map<String, Trade> getAllBestBuyTrades();
    Map<String, Trade> getAllBestSellTrades();
    List<Trade> getAllUserOpenTrades(Long customerId);
}
