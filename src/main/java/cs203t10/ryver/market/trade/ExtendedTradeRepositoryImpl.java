package cs203t10.ryver.market.trade;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import cs203t10.ryver.market.stock.Stock;

import static cs203t10.ryver.market.stock.StockException.NoSuchStockException;

public class ExtendedTradeRepositoryImpl implements ExtendedTradeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public Optional<Trade> findLatestBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND submitted_date = (",
                "SELECT MAX(submitted_date) FROM STOCK_RECORD",
                "WHERE stock_id = :stock_id",
            ")"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol);
        try {
            Trade result = (Trade) query.getSingleResult();
            return Optional.of(result);
        } catch (NoResultException | NonUniqueResultException e) {
            return Optional.empty();
        }
    }

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
        try {
            Stock stockRef = entityManager.getReference(Stock.class, symbol);
            trade.setStock(stockRef);
            return tradeRepo.save(trade);
        } catch (DataIntegrityViolationException e) {
            throw new NoSuchStockException(symbol);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Trade> findAllLatestPerStock() {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE t",
            "JOIN (",
                "SELECT MAX(submitted_date) AS latest_date, stock_id",
                "FROM TRADE",
                "GROUP BY stock_id",
            ") t2",
            "ON t.stock_id = t2.stock_id",
            "AND t.submitted_date = t2.latest_date"
        );
        Query query = entityManager.createNativeQuery(sql, Trade.class);
        return query.getResultList();
    }

    @Override
    public Long getTotalQuantityBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT IFNULL(SUM(quantity), 0) FROM TRADE",
            "WHERE stock_id = :stock_id"
        );
        Query query = entityManager.createNativeQuery(sql);
        BigInteger result = (BigInteger) query.setParameter("stock_id", symbol).getSingleResult();
        return result.longValue();
    }

    @Override
    public Long getBuyQuantityBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT IFNULL(SUM(quantity), 0) FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = 'BUY'"
        );
        Query query = entityManager.createNativeQuery(sql);
        BigInteger result = (BigInteger) query.setParameter("stock_id", symbol).getSingleResult();
        return result.longValue();
    }

    @Override
    public Long getSellQuantityBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT IFNULL(SUM(quantity), 0) FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = 'SELL'"
        );
        Query query = entityManager.createNativeQuery(sql);
        BigInteger result = (BigInteger) query.setParameter("stock_id", symbol).getSingleResult();
        return result.longValue();
    }

    @Override
    public Optional<Trade> findBestBuyBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = 'BUY'",
            "AND price = (",
                "SELECT MAX(price) FROM TRADE",
                "WHERE stock_id = :stock_id",
                "AND action = 'BUY'",
            ")",
            "ORDER BY submitted_date"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol);
        @SuppressWarnings("unchecked")
        List<Trade> result = query.getResultList();
        if (result.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public Optional<Trade> findBestSellBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = 'SELL'",
            "AND price = (",
                "SELECT MIN(price) FROM TRADE",
                "WHERE stock_id = :stock_id",
                "AND action = 'SELL'",
            ")",
            "ORDER BY submitted_date"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol);
        @SuppressWarnings("unchecked")
        List<Trade> result = query.getResultList();
        if (result.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public Map<String, Trade> findAllBestBuy() {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE t",
            "JOIN (",
                "SELECT MAX(price) AS best_price, stock_id",
                "FROM TRADE",
                "GROUP BY stock_id",
            ") t2",
            "ON t.stock_id = t2.stock_id",
            "AND t.price = t2.best_price",
            "WHERE t.action = 'BUY'",
            "ORDER BY submitted_date"
        );
        Query query = entityManager.createNativeQuery(sql, Trade.class);
        @SuppressWarnings("unchecked")
        List<Trade> trades = query.getResultList();
        Map<String, Trade> result = new HashMap<>();
        for (Trade trade : trades) {
            // Place the first (earliest) trade into the result.
            result.putIfAbsent(trade.getStock().getSymbol(), trade);
        }
        return result;
    }

    @Override
    public Map<String, Trade> findAllBestSell() {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE t",
            "JOIN (",
                "SELECT MIN(price) AS best_price, stock_id",
                "FROM TRADE",
                "GROUP BY stock_id",
            ") t2",
            "ON t.stock_id = t2.stock_id",
            "AND t.price = t2.best_price",
            "WHERE t.action = 'SELL'",
            "ORDER BY submitted_date"
        );
        Query query = entityManager.createNativeQuery(sql, Trade.class);
        @SuppressWarnings("unchecked")
        List<Trade> trades = query.getResultList();
        Map<String, Trade> result = new HashMap<>();
        for (Trade trade : trades) {
            // Place the first (earliest) trade into the result.
            result.putIfAbsent(trade.getStock().getSymbol(), trade);
        }
        return result;
    }

}

