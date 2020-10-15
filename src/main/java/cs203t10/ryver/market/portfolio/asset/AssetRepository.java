package cs203t10.ryver.market.portfolio.asset;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cs203t10.ryver.market.portfolio.asset.Asset;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
  public List<Asset> findByPortfolioCustomerId(Integer customerId);
  public List<Asset> findByCode(String code);
  public Asset findByPortfolioCustomerIdAndCode(Integer customerId, String code);
  public Asset deleteByPortfolioCustomerIdAndCode(Integer customerId, String code);
}