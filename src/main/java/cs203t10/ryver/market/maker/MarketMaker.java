package cs203t10.ryver.market.maker;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRepository;
import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.TradeRepository;
import cs203t10.ryver.market.trade.TradeService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;

@Service
public class MarketMaker {

    public static final int MIN_QUANTITY = 20_000;

    @Autowired
    StockRepository stockRepo;

    @Autowired
    TradeRepository tradeRepo;

    @Autowired
    TradeService tradeService;

    public void makeNewTrades() {
        List<Stock> stocks = stockRepo.findAll();
        for (Stock stock : stocks) {
            makeNewBuyTrades(stock.getSymbol());
            makeNewSellTrades(stock.getSymbol());
        }
    }

    public void makeNewBuyTrades(String symbol) {
        Long totalQuantity = tradeRepo.getBuyQuantityBySymbol(symbol);
        // If liquidity is low, then make new trades
        if (totalQuantity < MIN_QUANTITY) {
            tradeService.saveTrade(TradeView.builder()
                    .action(Action.BUY)
                    .symbol(symbol)
                    .quantity((int) (MIN_QUANTITY - totalQuantity))
                    .customerId(0)
                    .accountId(0)
                    .submittedDate(new Date())
                    .status(Status.OPEN)
                    .build());
        }
    }

    public void makeNewSellTrades(String symbol) {
        Long totalQuantity = tradeRepo.getSellQuantityBySymbol(symbol);
        // If liquidity is low, then make new trades
        if (totalQuantity < MIN_QUANTITY) {
            tradeService.saveTrade(TradeView.builder()
                    .action(Action.SELL)
                    .symbol(symbol)
                    .quantity((int) (MIN_QUANTITY - totalQuantity))
                    .customerId(0)
                    .accountId(0)
                    .submittedDate(new Date())
                    .status(Status.OPEN)
                    .build());
        }
    }

}

