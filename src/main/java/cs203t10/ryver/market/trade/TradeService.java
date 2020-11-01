package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.trade.view.TradeView;

public interface TradeService {
    Trade saveTrade(TradeView tradeView);
    Trade saveMarketMakerTrade(TradeView tradeView);
    Trade getTrade(Integer tradeId);
    List<Trade> getAllSellTradesBySymbol(String symbol);
    List<Trade> getAllBuyTradesBySymbol(String symbol);
    Integer getTotalBidVolume(String symbol);
    Integer getTotalAskVolume(String symbol);
    Trade getBestLimitBuyBySymbol(String symbol);
    Trade getBestLimitSellBySymbol(String symbol);
    Trade getBestBuyForStockView(String symbol);
    Trade getBestSellForStockView(String symbol);
    Trade getBestBuy(String symbol);
    Trade getBestSell(String symbol);
    Trade updateTrade(Trade trade);
    List<Trade> getAllUserOpenTrades(Long customerId);
    Trade cancelTrade(Integer tradeId);
    void resetTrades();
    void reconcileMarket(String symbol);
}
