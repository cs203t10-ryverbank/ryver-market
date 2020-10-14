package cs203t10.ryver.market.fund;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cs203t10.ryver.market.fund.exception.AccountNotAllowedException;
import cs203t10.ryver.market.fund.exception.InsufficientBalanceException;

@Service
public class FundTransferService {

    @Autowired
    private RestTemplate restTemplate;

    public void deductAvailableBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
    }

    public void deductBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
    }

    public void addBalance(Integer customerId, Integer accountId, Double amount)
            throws AccountNotAllowedException {
    }

}

