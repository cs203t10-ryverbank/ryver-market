package cs203t10.ryver.market.trade;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public Trade saveTrade(Trade trade) {
        return tradeRepo.save(trade);
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return tradeRepo.findById(tradeId).orElse(null);
    }

}   