package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class ExtendedStockRecordRepositoryImpl implements ExtendedStockRecordRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StockRecord> findAllBySymbol(String symbol) {
        String jpql = "FROM StockRecord WHERE stock_id = :stock_id";
        TypedQuery<StockRecord> query = entityManager.createQuery(jpql, StockRecord.class);
        return query
            .setParameter("stock_id", symbol)
            .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StockRecord> findAllLatestPerStock() {
        final String sql = String.join(" ",
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
        return query.getResultList();
    }

    @Override
    public Optional<StockRecord> findLatestBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM STOCK_RECORD",
            "WHERE stock_id = :stock_id",
            "AND submitted_date = (",
                "SELECT MAX(submitted_date) FROM STOCK_RECORD",
                "WHERE stock_id = :stock_id",
            ")"
        );
        Query query = entityManager
                .createNativeQuery(sql, StockRecord.class)
                .setParameter("stock_id", symbol);
        try {
            StockRecord result = (StockRecord) query.getSingleResult();
            return Optional.of(result);
        } catch (NoResultException | NonUniqueResultException e) {
            return Optional.empty();
        }
    }

}

