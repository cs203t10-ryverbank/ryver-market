package cs203t10.ryver.market.trade;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.trade.Trade.Action;

import cs203t10.ryver.market.stock.exception.*;

public class ExtendedTradeRepositoryImpl implements ExtendedTradeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TradeRepository tradeRepo;

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
    public Optional<Trade> findLatestBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND submitted_date = (",
                "SELECT MAX(submitted_date) FROM TRADE",
                "WHERE stock_id = :stock_id",
            ")"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol);
        @SuppressWarnings("unchecked")
        List<Trade> results = (List<Trade>) query.getResultList();
        if (results.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Override
    public List<Trade> findAllByCustomerId(Long customerId) {
        final String jpql = "FROM Trade WHERE customer_id = :customer_id";
        TypedQuery<Trade> query = entityManager.createQuery(jpql, Trade.class);
        return query
            .setParameter("customer_id", customerId)
            .getResultList();
    }

    @Override
    public List<Trade> findAllSellTradesBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = :action"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol)
                .setParameter("action", (Action.SELL).toString().toUpperCase());
        @SuppressWarnings("unchecked")
        List<Trade> results = query.getResultList();
        System.out.println(results.size());
        return results;
    }

    @Override
    public List<Trade> findAllBuyTradesBySymbol(String symbol) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = :action"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol)
                .setParameter("action", (Action.BUY).toString().toUpperCase());
        @SuppressWarnings("unchecked")
        List<Trade> results = query.getResultList();
        System.out.println(results.size());
        return results;
    }

    @Override
    public Optional<Trade> findBestMarketBuyBySymbol(String symbol) {
        return findBestMarketBySymbol(symbol, Action.BUY);
    }

    @Override
    public Optional<Trade> findBestMarketSellBySymbol(String symbol) {
        return findBestMarketBySymbol(symbol, Action.SELL);
    }

    /**
     * The best market trade is determined by submitted date.
     */
    private Optional<Trade> findBestMarketBySymbol(String symbol, Action action) {
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = :action",
            "AND price = 0",
            "AND status != 'FILLED'",
            "AND status != 'CANCELLED'",
            "AND status != 'EXPIRED'",
            "AND STATUS != 'INVALID'",
            "ORDER BY submitted_date"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol)
                .setParameter("action", action.toString().toUpperCase());
        @SuppressWarnings("unchecked")
        List<Trade> result = query.getResultList();
        if (result.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public Optional<Trade> findBestLimitBuyBySymbol(String symbol) {
        return findBestLimitBySymbol(symbol, Action.BUY);
    }

    @Override
    public Optional<Trade> findBestLimitSellBySymbol(String symbol) {
        return findBestLimitBySymbol(symbol, Action.SELL);
    }

    private Optional<Trade> findBestLimitBySymbol(String symbol, Action action) {
        String bestFunction = action.equals(Action.BUY) ? "MAX" : "MIN";
        // TODO: edit status to use query manager
        final String sql = String.join(" ",
            "SELECT * FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = :action",
            "AND price = (",
                "SELECT " + bestFunction + "(price) FROM TRADE",
                "WHERE stock_id = :stock_id",
                "AND action = :action",
                "AND price <> 0",
                "AND STATUS != 'FILLED'",
                "AND STATUS != 'CANCELLED'",
                "AND STATUS != 'EXPIRED'",
                "AND STATUS != 'INVALID'",
            ")",
            "AND status != 'FILLED'",
            "ORDER BY submitted_date"
        );
        Query query = entityManager
                .createNativeQuery(sql, Trade.class)
                .setParameter("stock_id", symbol)
                .setParameter("action", action.toString().toUpperCase());
        @SuppressWarnings("unchecked")
        List<Trade> result = query.getResultList();
        if (result.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public Long getBuyQuantityBySymbol(String symbol) {
        return getQuantityBySymbol(symbol, Action.BUY);
    }

    @Override
    public Long getBuyFilledQuantityBySymbol(String symbol) {
        return getFilledQuantityBySymbol(symbol, Action.BUY);
    }

    @Override
    public Long getSellQuantityBySymbol(String symbol) {
        return getQuantityBySymbol(symbol, Action.SELL);
    }

    @Override
    public Long getSellFilledQuantityBySymbol(String symbol) {
        return getFilledQuantityBySymbol(symbol, Action.BUY);
    }

    private Long getQuantityBySymbol(String symbol, Action action) {
        final String sql = String.join(" ",
            "SELECT IFNULL(SUM(quantity), 0) FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = :action"
        );
        Query query = entityManager.createNativeQuery(sql);
        BigInteger result = (BigInteger) query
                .setParameter("stock_id", symbol)
                .setParameter("action", action.toString().toUpperCase())
                .getSingleResult();
        return result.longValue();
    }

    private Long getFilledQuantityBySymbol(String symbol, Action action) {
        final String sql = String.join(" ",
            "SELECT IFNULL(SUM(filled_quantity), 0) FROM TRADE",
            "WHERE stock_id = :stock_id",
            "AND action = :action",
            "AND status != 'FILLED'",
            "AND status != 'CANCELLED'",
            "AND status != 'EXPIRED'"
        );
        Query query = entityManager.createNativeQuery(sql);
        BigInteger result = (BigInteger) query
                .setParameter("stock_id", symbol)
                .setParameter("action", action.toString().toUpperCase())
                .getSingleResult();
        return result.longValue();
    }

}

