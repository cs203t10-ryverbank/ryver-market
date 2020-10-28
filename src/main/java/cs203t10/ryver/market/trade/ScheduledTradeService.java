package cs203t10.ryver.market.trade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRepository;
import cs203t10.ryver.market.trade.Trade.Status;

@Component
@Service
public final class ScheduledTradeService {

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TradeRepository tradeRepo;

    @Autowired
    private FundTransferService fundTransferService;

    // TODO: Verify this works on AWS
    @Scheduled(cron = "0 0 09 * * MON-FRI", zone = "Asia/Singapore")
    public void openMarket() {
        List<Stock> allStocks = stockRepo.findAll();
        for (Stock stock : allStocks) {
            tradeService.reconcileMarket(stock.getSymbol());
        }
    }

    // TODO: Verify this works on AWS
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "Asia/Singapore")
    public void closeMarket() {
        // Cron expression: close market at 5pm from Monday to Friday.
        List<Trade> tradeList = tradeRepo.findAll();
        Set<List<Integer>> customerAccountSet = new HashSet<>();

        // Close trades for all trades in the tradeList
        for (Trade trade : tradeList) {
            // TODO: Implement tradeRepo.findAllNonExpired() instead.
            if (trade.getStatus().equals(Status.EXPIRED)) {
                continue;
            }

            // Expire trades.
            trade.setStatus(Status.EXPIRED);
            tradeRepo.save(trade);

            Integer customerId = trade.getCustomerId();
            Integer accountId = trade.getAccountId();
            // Set will only store unique instances of { customerId, accountId }.
            customerAccountSet.add(List.of(customerId, accountId));
        }

        // For each { customerId, accountId } pair, reset available balance.
        for (List<Integer> pair : customerAccountSet) {
            Integer customerId = pair.get(0);
            Integer accountId = pair.get(1);
            // Reset balance using FTS
            fundTransferService.resetAvailableBalance(customerId, accountId);
        }
    }

}


