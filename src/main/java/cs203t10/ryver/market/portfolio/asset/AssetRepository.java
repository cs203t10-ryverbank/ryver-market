package cs203t10.ryver.market.portfolio.asset;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    List<Asset> findByPortfolioCustomerId(Integer customerId);
    List<Asset> findByCode(String code);
    Optional<Asset> findByPortfolioCustomerIdAndCode(Integer customerId, String code);
}

