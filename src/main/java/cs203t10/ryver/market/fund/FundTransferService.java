package cs203t10.ryver.market.fund;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import cs203t10.ryver.market.fund.exception.AccountNotAllowedException;
import cs203t10.ryver.market.fund.exception.InsufficientBalanceException;
import cs203t10.ryver.market.security.SecurityUtils;
import static cs203t10.ryver.market.security.SecurityConstants.AUTH_HEADER_KEY;
import static cs203t10.ryver.market.security.SecurityConstants.BEARER_PREFIX;
import static cs203t10.ryver.market.security.SecurityConstants.MARKET_JWT;

@Service
public class FundTransferService {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    private String getFtsHostUrl() {
        // Find an instance of the ryver market service
        List<ServiceInstance> instances = discoveryClient.getInstances("ryver-fts");
        if (instances.size() == 0) {
            //throw new NoInstanceException("ryver-market");
            System.out.println("no ryver-fts");
        }

        return instances.get(0).getUri().toString();
    }

    private String getAccountsUrl() {
        return getFtsHostUrl() + "/accounts";
    }

    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();

        //set header to AUTH: Bearer ...
        headers.set(AUTH_HEADER_KEY, BEARER_PREFIX + MARKET_JWT);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
    }

    private HttpEntity<String> getUserHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        String jwt = SecurityUtils.getCurrentSessionJWT();

        //set header to AUTH: Bearer ...
        headers.set(AUTH_HEADER_KEY, BEARER_PREFIX + jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
    }


    public void deductAvailableBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
        String url = getAccountsUrl();
        HttpEntity<String> req = getUserHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/deductAvailableBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Deduct Available Balance: " + response.getBody());
    }

    public void addAvailableBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
        String url = getAccountsUrl();
        HttpEntity<String> req = getHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/{customerId}/addAvailableBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Deduct Available Balance: " + response.getBody());
    }

    public void deductBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
        String url = getAccountsUrl();
        HttpEntity<String> req = getHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/{customerId}/deductBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Deduct Balance: " + response.getBody());
    }

    public void addBalance(Integer customerId, Integer accountId, Double amount)
            throws AccountNotAllowedException {
        String url = getAccountsUrl();
        HttpEntity<String> req = getHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/{customerId}/addBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Add Balance: " + response.getBody());
    }

    public void resetAvailableBalance(Integer customerId, Integer accountId)
    throws AccountNotAllowedException {
        String url = getAccountsUrl();
        HttpEntity<String> req = getHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/{customerId}/resetAvailableBalance", HttpMethod.PUT, req, String.class, accountId);

        //for debugging
        System.out.println("Add Balance: " + response.getBody());
    }

}

