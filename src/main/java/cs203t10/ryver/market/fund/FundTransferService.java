package cs203t10.ryver.market.fund;

import javax.servlet.http.HttpServletRequest;
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
import static cs203t10.ryver.market.security.SecurityConstants.BASIC_PREFIX;
import static cs203t10.ryver.market.security.SecurityConstants.BEARER_PREFIX;

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

        return instances.get(0).getUri().toString() + "/accounts";
    }

    private HttpEntity getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        String jwt = SecurityUtils.getJWT();

        //set header to AUTH: Bearer ...
        headers.set(AUTH_HEADER_KEY, BEARER_PREFIX  + jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity <String> entity = new HttpEntity<>(headers);

       return entity;
    }

    public void deductAvailableBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
        // TODO: fix
        String url = getFtsHostUrl();
        HttpEntity<String> req = getHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/deductAvailableBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Deduct Available Balance: " + response.getBody());
    }

    public void deductBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
        // TODO: fix
        String url = getFtsHostUrl();
        HttpEntity<String> req = getHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/deductBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Deduct Balance: " + response.getBody());
    }

    public void addBalance(Integer customerId, Integer accountId, Double amount)
            throws AccountNotAllowedException {
        // TODO: fix
        String url = getFtsHostUrl();
        ResponseEntity<String> req = null;
        ResponseEntity<String> response = restTemplate.exchange(url + "/{accountId}/addBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

        //for debugging
        System.out.println("Add Balance: " + response.getBody());
    }

}

