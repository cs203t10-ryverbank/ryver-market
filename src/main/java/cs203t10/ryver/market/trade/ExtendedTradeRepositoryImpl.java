package cs203t10.ryver.market.trade;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class ExtendedTradeRepositoryImpl implements ExtendedTradeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Trade> findAllByCustomerId(Long customerId) {
        String jpql = "FROM Trade WHERE customer_id = :customer_id";
        TypedQuery<Trade> query = entityManager.createQuery(jpql, Trade.class);
        return query
            .setParameter("customer_id", customerId)
            .getResultList();
    }
}

