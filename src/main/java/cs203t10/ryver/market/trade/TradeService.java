package cs203t10.ryver.market.trade;

public interface TradeService {
    Trade saveTrade (Trade trade);
    Trade getTrade (Integer tradeId);
}
