package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.trade.view.TradeView;

public interface TradeService {
    Trade saveTrade(TradeView tradeView);
    Trade getTrade(Integer tradeId);
    Trade getBestBuy(String symbol);
    Trade getBestSell(String symbol);
    Trade getBestMarketBuyBySymbol(String symbol);
    Trade getBestMarketSellBySymbol(String symbol);
    Trade getBestLimitBuyBySymbol(String symbol);
    Trade getBestLimitSellBySymbol(String symbol);
    Trade updateTrade(Trade trade);
    List<Trade> getAllUserOpenTrades(Long customerId);
    void deleteTrade(Integer tradeId);
}
