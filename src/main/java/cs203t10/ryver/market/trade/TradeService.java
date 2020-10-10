package cs203t10.ryver.market.trade;

import java.util.List;

import cs203t10.ryver.market.stock.Stock;

public interface TradeService {
    Trade saveTrade (Trade trade);
    Trade getTrade (Integer tradeId);
}
