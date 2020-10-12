package cs203t10.ryver.market.stock;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class ExtendedStockRecordRepositoryImpl implements ExtendedStockRecordRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StockRecord> findAllByStockSymbol(String symbol) {
        String jpql = "FROM StockRecord WHERE stock_id = :stock_id";
        TypedQuery<StockRecord> query = entityManager.createQuery(jpql, StockRecord.class);
        return query
            .setParameter("stock_id", symbol)
            .getResultList();
    }

    @Override
    public List<StockRecord> findAllLatestStockRecords() {
        String sql = String.join(" ",
                "SELECT * FROM STOCK_RECORD sr",
                "JOIN (",
                "SELECT MAX(submitted_date) AS latest_date, stock_id",
                "FROM STOCK_RECORD",
                "GROUP BY stock_id",
                ") sr2",
                "ON sr.stock_id = sr2.stock_id",
                "AND sr.submitted_date = sr2.latest_date"
        );
        Query query = entityManager.createNativeQuery(sql, StockRecord.class);
        @SuppressWarnings("unchecked")
        List<StockRecord> result = query.getResultList();
        return result;
    }

}

