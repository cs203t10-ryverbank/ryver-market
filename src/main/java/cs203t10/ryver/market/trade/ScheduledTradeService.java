package cs203t10.ryver.market.trade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.portfolio.asset.AssetService;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.util.DateService;

@Component
@Service
public class ScheduledTradeService {

    @Autowired
    private ReconciliationService reconService;

    @Autowired
    private TradeRepository tradeRepo;

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DateService dateService;

    // TODO: Verify this works on AWS
    // Cron expression: open market at 9am from Monday to Friday.
    @Scheduled(cron = "0 0 09 * * MON-FRI", zone = "Asia/Singapore")
    public void openMarketIfDefault() {
        if (!dateService.isArtificial()) {
            openMarket();
        }
    }

    public void openMarket() {
        List<Trade> tradeList = tradeRepo.findAllClosedTrades();

        // Update the stock record for any open sell trade.
        for (Trade trade : tradeList) {
            trade.setStatus(Status.OPEN);
            tradeRepo.save(trade);
            reconService.reconcileMarket(trade.getStock().getSymbol());
        }

    }

    // TODO: Verify this works on AWS
    // Cron expression: close market at 5pm from Monday to Friday.
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "Asia/Singapore")
    public void closeMarketIfDefault() {
        if (!dateService.isArtificial()) {
            closeMarket();
        }
    }

    public void closeMarket() {
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
            if (accountId == 0 && customerId == 0){
                // Do not reset if it is a market maker trade.
                continue;
            }
            fundTransferService.resetAvailableBalance(customerId, accountId);
        }

        // Reset available quantity of all assets
        assetService.resetAssetAvailableQuantity();
    }

}


