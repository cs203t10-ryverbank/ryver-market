package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.trade.view.TradeView;

public interface TradeService {
    Trade saveTrade(TradeView tradeView);
    Trade getTrade(Integer tradeId);
    Trade getBestMarketBuyBySymbol(String symbol);
    Trade getBestMarketSellBySymbol(String symbol);
    Trade getBestLimitBuyBySymbol(String symbol);
    Trade getBestLimitSellBySymbol(String symbol);
    List<Trade> getAllUserOpenTrades(Long customerId);
}
