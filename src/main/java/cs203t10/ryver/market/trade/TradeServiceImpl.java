package cs203t10.ryver.market.trade;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.view.TradeView;

import static cs203t10.ryver.market.trade.TradeException.TradeNotFoundException;

import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public Trade saveTrade(Trade trade) {
        return tradeRepo.save(trade);
    }

    @Override
    public Trade saveTrade(TradeView tradeView) {
        Trade trade = new Trade();
        BeanUtils.copyProperties(tradeView, trade);
        return tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return tradeRepo.findById(tradeId)
                    .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public List<Trade> getAllUserOpenTrades(Long customerId) {
        return tradeRepo.findAllByCustomerId(customerId);
    }

}
