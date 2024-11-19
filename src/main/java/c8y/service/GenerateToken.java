package c8y.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.cumulocity.microservice.subscription.repository.application.ApplicationApi;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.sdk.client.RestConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import c8y.model.TenantDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GenerateToken {

    private final ApplicationApi applicationApi;
    private final RestConnector restConnector;
    private final RestTemplate restTemplate;
    private final MicroserviceSubscriptionsService subscriptionService;
    private final Environment environment;

    public GenerateToken(ApplicationApi applicationApi, MicroserviceSubscriptionsService subscriptionService,
     RestConnector restConnector, RestTemplate restTemplate, Environment environment){
        this.applicationApi=applicationApi;
        this.subscriptionService= subscriptionService;
        this.restConnector= restConnector;
        this.restTemplate=restTemplate;
        this.environment= environment;
    }

    public List<TenantDetails> createToken() {
        List<TenantDetails> token = new ArrayList<>();
        String bootstrapUser=environment.getProperty("C8Y.bootstrap.user");
        String bootstrapTenantId= environment.getProperty("C8Y.bootstrap.tenant");
        String BootStrapPassword = environment.getProperty("C8Y.bootstrap.password");
        String baseUrl= environment.getProperty("C8Y.baseURL");
        createAuthToken(baseUrl+"/tenant/oauth/token?tenant_Id="+bootstrapTenantId,bootstrapTenantId,
        bootstrapUser, BootStrapPassword, "");
        //restTemplate.exchange(null, null, null, null, null)
        return subscriptionService.callForTenant(subscriptionService.getTenant(), ()->{
            try{
                //String res= restConnector.get("/application/currentApplication/subscriptions", MediaType.APPLICATION_JSON_TYPE, String.class);
                //log.info("App: {}", res);
                return token;
            }catch(Exception e){
                log.error("Error message: {}", e.getMessage());
                return token;

            }
        });
    }

    private String createAuthToken(String url, String tenantId, String user, String password, String authToken){
        String token = "";
        try{
            // Set up headers
            log.info("URL: {}", environment.getProperty("C8Y.baseURL"));
            log.info("user: {}", environment.getProperty("C8Y.bootstrap.user"));
            log.info("password: {}", environment.getProperty("C8Y.bootstrap.password"));
            log.info("tenant: {}", environment.getProperty("C8Y.bootstrap.tenant"));
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
                formParams.add("password", environment.getProperty("C8Y.bootstrap.password"));
                formParams.add("username", environment.getProperty("C8Y.bootstrap.tenant")+"/"+environment.getProperty("C8Y.bootstrap.user"));
                formParams.add("grant_type", "PASSWORD");
                // Create the request entity with headers and body
                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formParams, headers);
                ResponseEntity<String> res= restTemplate.exchange(environment.getProperty("C8Y.baseURL")+"/tenant/oauth/token?tenant_Id="+tenantId, HttpMethod.POST, requestEntity, String.class);
                log.info("App: {}", res.getBody());
                return res.getBody();
            }catch(Exception e){
                log.error("Error message: {}", e.getMessage());
                return token;

            }
    }

}
