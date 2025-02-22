package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.trade.view.TradeViewCreatable;

public interface TradeService {
    Trade saveTrade(TradeViewCreatable tradeView);
    Trade saveMarketMakerTrade(TradeViewCreatable tradeView);
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
    void completeBuyTrade(Trade trade, Double totalPrice);
    void completeSellTrade(Trade trade, Double totalPrice);
    List<Trade> getAllUserOpenTrades(Long customerId);
    Trade cancelTrade(Trade trade);
    Trade cancelTrade(Integer tradeId);
    void resetTrades();
    Trade addTradeToOpenMarket(TradeViewCreatable tradeView, Double availableBalance);
    Double determineTransactedPriceIfBothMarketOrders(Trade bestSell, Trade bestBuy);
    Double determineTransactedPriceIfBothLimitOrders(Trade bestSell, Trade bestBuy);
}

