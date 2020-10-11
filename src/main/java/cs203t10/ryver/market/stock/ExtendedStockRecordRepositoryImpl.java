package cs203t10.ryver.market.stock;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class ExtendedStockRecordRepositoryImpl implements ExtendedStockRecordRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StockRecord> findAllByStockSymbol(String symbol) {
        String jpql = "FROM StockRecord WHERE stock_id = :stock_id";

        TypedQuery<StockRecord> query = entityManager.createQuery(jpql, StockRecord.class);
        query.setParameter("stock_id", symbol);

        return query.getResultList();
    }

}

