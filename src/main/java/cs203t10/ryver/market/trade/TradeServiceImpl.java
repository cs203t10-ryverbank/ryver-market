package cs203t10.ryver.market.trade;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.TradeException.TradeNotFoundException;
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
    public Map<String, Trade> getAllBestBuyTrades() {
        return tradeRepo.findAllBestBuy();
    }

    @Override
    public Map<String, Trade> getAllBestSellTrades() {
        return tradeRepo.findAllBestSell();
    }

    @Override
    public List<Trade> getAllUserOpenTrades(Long customerId) {
        return tradeRepo.findAllByCustomerId(customerId);
    }

}
