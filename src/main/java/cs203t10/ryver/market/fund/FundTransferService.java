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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import cs203t10.ryver.market.fund.exception.AccountNotAllowedException;
import cs203t10.ryver.market.fund.exception.InsufficientBalanceException;
import static cs203t10.ryver.market.security.SecurityConstants.AUTH_HEADER_KEY;
import static cs203t10.ryver.market.security.SecurityConstants.BASIC_PREFIX;

@Service
public class FundTransferService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    HttpServletRequest request;

    @Autowired
    DiscoveryClient discoveryClient;

    //private final String REQUEST_URI = "http://localhost:8080/accounts/";

    private String prepURI(){
         // Find an instance of the ryver market service
         List<ServiceInstance> instances = discoveryClient.getInstances("ryver-market");
         if (instances.size() == 0) {
             //throw new NoInstanceException("ryver-market");
             System.out.println("no ryver-market");
         }
         String url = instances.get(0).getUri().toString();
         String accountsUrl = url + "accounts/";
         return accountsUrl;
    }

    //Preps the Request Body for restTemplate 
    private HttpEntity<String> prepRequest(){
        String authHeader = request.getHeader(AUTH_HEADER_KEY);
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER_KEY, authHeader);
        HttpEntity<String> req = new HttpEntity<>("", headers);
        return req;
    }

    public void deductAvailableBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
                String uri = prepURI();
                HttpEntity<String> req = prepRequest();
                ResponseEntity<String> response = restTemplate.exchange(uri + "{accountId}/deductAvailableBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);

                //for debugging
                System.out.println("Deduct Available Balance: " + response.getBody());
    }

    public void deductBalance(Integer customerId, Integer accountId, Double amount)
            throws InsufficientBalanceException, AccountNotAllowedException {
                String uri = prepURI();
                HttpEntity<String> req = prepRequest();
                ResponseEntity<String> response = restTemplate.exchange(uri + "{accountId}/deductBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);
                
                //for debugging
                System.out.println("Deduct Balance: " + response.getBody());
    }

    public void addBalance(Integer customerId, Integer accountId, Double amount)
            throws AccountNotAllowedException {
                String uri = prepURI();
                HttpEntity<String> req = prepRequest();
                ResponseEntity<String> response = restTemplate.exchange(uri + "{accountId}/addBalance?amount={amount}", HttpMethod.PUT, req, String.class, accountId, amount);
                
                //for debugging
                System.out.println("Add Balance: " + response.getBody());
    }

}

