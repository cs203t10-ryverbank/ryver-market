package cs203t10.ryver.market.trade;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.exception.*;
import cs203t10.ryver.market.trade.view.TradeView;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public Trade saveTrade(TradeView tradeView) {
        // If placing a buy order, the user's available balance must be
        // deducted.
        if (tradeView.getAction() == Action.BUY) {
            registerBuyTrade(tradeView);
        }
        Trade trade = tradeView.toTrade();
        return tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());
    }

    private void registerBuyTrade(TradeView tradeView) {
        fundTransferService.deductAvailableBalance(
                tradeView.getCustomerId(),
                tradeView.getAccountId(),
                tradeView.getBid() * tradeView.getQuantity()
        );
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return tradeRepo.findById(tradeId)
                    .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public Trade getBestBuy(String symbol) {
        Trade bestMarket = getBestMarketBuyBySymbol(symbol);
        Trade bestLimit = getBestLimitBuyBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) return null;
        if (bestLimit == null) return bestMarket;
        if (bestMarket == null) return bestLimit;
        // The buy with a higher price is better, as it gives the
        // matcher (seller) more per stock traded.
        if (bestLimit.getPrice() > bestMarket.getPrice()) {
            return bestLimit;
        } else if (bestLimit.getPrice() < bestMarket.getPrice()) {
            return bestMarket;
        }
        // If price is the same, then the earlier buy is returned.
        if (bestLimit.getSubmittedDate().before(bestMarket.getSubmittedDate())) {
            return bestLimit;
        }
        return bestMarket;
    }

    @Override
    public Trade getBestSell(String symbol) {
        Trade bestMarket = getBestMarketSellBySymbol(symbol);
        Trade bestLimit = getBestLimitSellBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) return null;
        if (bestLimit == null) return bestMarket;
        if (bestMarket == null) return bestLimit;
        // The sell with a lower price is better, as it lets the
        // matcher (buyer) get more stocks for a lower price.
        if (bestLimit.getPrice() < bestMarket.getPrice()) {
            return bestLimit;
        } else if (bestLimit.getPrice() > bestMarket.getPrice()) {
            return bestMarket;
        }
        // If price is the same, then the earlier sell is returned.
        if (bestLimit.getSubmittedDate().before(bestMarket.getSubmittedDate())) {
            return bestLimit;
        }
        return bestMarket;
    }


    @Override
    public Trade getBestMarketBuyBySymbol(String symbol) {
        return tradeRepo.findBestMarketBuyBySymbol(symbol).orElse(null);
    }

    @Override
    public Trade getBestMarketSellBySymbol(String symbol) {
        return tradeRepo.findBestMarketSellBySymbol(symbol).orElse(null);
    }

    @Override
    public Trade getBestLimitBuyBySymbol(String symbol) {
        return tradeRepo.findBestLimitBuyBySymbol(symbol).orElse(null);
    }

    @Override
    public Trade getBestLimitSellBySymbol(String symbol) {
        return tradeRepo.findBestLimitSellBySymbol(symbol).orElse(null);
    }

    @Override
    public List<Trade> getAllUserOpenTrades(Long customerId) {
        return tradeRepo.findAllByCustomerId(customerId);
    }

}
