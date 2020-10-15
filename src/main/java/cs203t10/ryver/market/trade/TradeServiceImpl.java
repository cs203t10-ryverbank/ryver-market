package cs203t10.ryver.market.trade;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.exception.TradeNotFoundException;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public Trade saveTrade(TradeView tradeView) {
        // Check if it is market order or limit order.
        if (tradeView.getAction() == Action.BUY) {
            if (tradeView.getBid() == 0){
                tradeView = registerMarketBuy(tradeView);
            }else{
                registerLimitBuy(tradeView);
            }
        }
        //save trade
        Trade trade = tradeView.toTrade();
        tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        // Reconcile market status after adding trade.
        trade = reconcileMarket(trade, tradeView.getSymbol());
        return trade;
    }

    private Trade reconcileMarket(Trade trade, String symbol){
        // TODO: DEDUCT + ADD ACTUAL BALANCE
        // TODO: MATCH BY TIME 
        if (trade.getAction() == Action.BUY) {
            Trade bestSell = getBestSell(symbol);

            if (bestSell == null){
                trade.setStatus(Status.OPEN);
                return trade;
            }

            Integer sellQuantity = bestSell.getQuantity() - bestSell.getFilledQuantity();
            Integer buyQuantity = trade.getQuantity() - trade.getFilledQuantity();
            if (sellQuantity > buyQuantity){
                trade.setFilledQuantity(trade.getQuantity());
                bestSell.setFilledQuantity(bestSell.getFilledQuantity() + buyQuantity);
                updateTrade(bestSell);
            } else{
                trade.setFilledQuantity(trade.getFilledQuantity() + sellQuantity);
                bestSell.setFilledQuantity(bestSell.getFilledQuantity() + sellQuantity);
                updateTrade(bestSell);
            }

            updateTrade(trade);
            return trade;
        }
        return trade;
    }

    private TradeView registerMarketBuy(TradeView tradeView) {
        // Market order: set trade bid to market best ask
        Trade bestSell = getBestSell(tradeView.getSymbol());
        if (bestSell != null) {
            Double bestSellPrice = bestSell.getPrice();
            tradeView.setBid(bestSellPrice);
        }
        registerBuyTrade(tradeView);
        return tradeView;
    }

    private void registerLimitBuy(TradeView tradeView) {
        registerBuyTrade(tradeView);
    }

    /**
     * Register a buy trade on the fund transfer service by deducting the
     * available balance of the appropriate user.
     *
     * The market maker does not need to register trades.
     *
     * The market maker has customerId = 0 and accountId = 0.
     */
    private void registerBuyTrade(TradeView tradeView) {
        Integer customerId = tradeView.getCustomerId();
        Integer accountId = tradeView.getAccountId();
        if (customerId == 0 && accountId == 0) {
            return;
        }

        fundTransferService.deductAvailableBalance(
                customerId, accountId,
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

    @Override
    public Trade updateTrade(Trade newTrade) {
        Integer tradeId = newTrade.getId();
        return tradeRepo.findById(tradeId).map(trade -> {
            trade.setFilledQuantity(newTrade.getFilledQuantity());
            System.out.println("filled: " + trade.getFilledQuantity());
            System.out.println("total: " + trade.getQuantity());
            // Set status
            if (trade.getFilledQuantity() == trade.getQuantity()){
                trade.setStatus(Status.FILLED);
            } else if (trade.getFilledQuantity() > 0){
                trade.setStatus(Status.PARTIAL_FILLED);
            } else {
                trade.setStatus(Status.OPEN);
            }
            return tradeRepo.save(trade);
        }).orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public void deleteTrade(Integer tradeId) {
        tradeRepo.delete(getTrade(tradeId));
    }

}
