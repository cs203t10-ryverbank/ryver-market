package cs203t10.ryver.market.trade;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;

import cs203t10.ryver.market.stock.Stock;

public class ExtendedTradeRepositoryImpl implements ExtendedTradeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public List<Trade> findAllByCustomerId(Long customerId) {
        String jpql = "FROM Trade WHERE customer_id = :customer_id";
        TypedQuery<Trade> query = entityManager.createQuery(jpql, Trade.class);
        return query
            .setParameter("customer_id", customerId)
            .getResultList();
    }

    @Override
    public Trade saveWithSymbol(Trade trade, String symbol) {
        Stock stockRef = entityManager.getReference(Stock.class, symbol);
        trade.setStock(stockRef);
        return tradeRepo.save(trade);
    }
}

