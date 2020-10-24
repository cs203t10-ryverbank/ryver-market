package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.trade.view.TradeView;

public interface TradeService {
    Trade saveTrade(TradeView tradeView);
    Trade saveMarketMakerTrade(TradeView tradeView);
    Trade getTrade(Integer tradeId);
    Trade getBestBuy(String symbol);
    Trade getBestSell(String symbol);
    Trade updateTrade(Trade trade);
    List<Trade> getAllUserOpenTrades(Long customerId);
    Trade cancelTrade(Integer tradeId);
}
