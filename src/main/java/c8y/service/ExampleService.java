package c8y.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.MediaTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import c8y.IsDevice;
import c8y.model.LoginMode;
import lombok.extern.slf4j.Slf4j;

import com.cumulocity.microservice.subscription.model.MicroserviceSubscriptionAddedEvent;
import com.cumulocity.microservice.subscription.repository.application.ApplicationApi;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.model.ID;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.model.option.OptionPK;
import com.cumulocity.rest.representation.application.ApplicationRepresentation;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.tenant.OptionRepresentation;
import com.cumulocity.rest.representation.user.UserRepresentation;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.identity.IdentityApi;
import com.cumulocity.sdk.client.inventory.BinariesApi;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.cumulocity.sdk.client.inventory.InventoryFilter;
import com.cumulocity.sdk.client.inventory.ManagedObjectCollection;
import com.cumulocity.sdk.client.option.TenantOptionApi;
import com.cumulocity.sdk.client.user.UserApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.cumulocity.sdk.client.RestConnector;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;


/**
 * This is an example service. This should be removed for your real project!
 * 
 * @author APES
 *
 */

 @Slf4j
 @Service
 public class ExampleService {

    @Value("${C8Y.bootstrap.tenant}")
	private String enterpriseTenantId;

    @Value("${category}")
	private String category;

    @Value("${key}")
	private String key;

    @Value("${application.name}")
	private String appName;

    private String errMsg = "Error Message: {}";

    private final InventoryApi inventoryApi;

    private final BinariesApi binariesApi;

    private final TenantOptionApi tenantOptionApi;

    private final IdentityApi identityApi;

    private final ApplicationApi applicationApi;

    private MicroserviceSubscriptionsService subscriptionService;

 	private RestConnector restConnector;

    private UserApi userApi;

    private ObjectMapper mapper;

    private int DEFAULT_BUFFER_SIZE =8192;



    public ExampleService(InventoryApi inventoryApi, TenantOptionApi tenantOptionApi,
     BinariesApi binariesApi, IdentityApi identityApi, ApplicationApi applicationApi,RestConnector restConnector,
     UserApi userApi, MicroserviceSubscriptionsService subscriptionService, ObjectMapper mapper) {
        this.inventoryApi = inventoryApi;
        this.tenantOptionApi = tenantOptionApi;
        this.binariesApi = binariesApi;
        this.identityApi = identityApi;
        this.applicationApi = applicationApi;
        this.restConnector = restConnector;
        this.subscriptionService =subscriptionService;
        this.userApi = userApi;
        this.mapper =mapper;
    }
 
    @EventListener
    public void onSubscriptionAdded(MicroserviceSubscriptionAddedEvent event) throws Exception {
        log.info("Subscription added for Tenant ID: <{}> ", event.getCredentials().getTenant()); 
        log.info("Subscription added for user ID: {} ", event.getCredentials().getUsername());
        log.info("Subscription added for password: {} ", event.getCredentials().getPassword());
        log.info("Subscription added for password: {}, {} ", getApplication(event.getCredentials().getTenant()));
        log.info("Bootstrap User: {}", event.getCredentials());
        log.info("Bootstrap password: {}", event.getCredentials());
        log.info("Bootstrap tenantId: {}", event.getCredentials());
        jsonQuery();
        //getEvent(event.getCredentials().getTenant());
        //getUser(event.getCredentials().getTenant());
        
        if(Boolean.TRUE.equals(getTenant(event.getCredentials().getTenant()))){
            log.info("Tenant Initialization already done for the tenant Id: {}",event.getCredentials().getTenant());
        }else{
            log.info("This is the newly created Tenant, Start default initialization service: {}",event.getCredentials().getTenant());
            //Service logic
            updateTenant(event.getCredentials().getTenant());
            log.info("Tenant Id {} updated within tenant Options",event.getCredentials().getTenant());
        }

    }

    public void jsonQuery() throws JsonProcessingException{
        ArrayNode jList = mapper.createArrayNode();
        jList.add(mapper.createObjectNode()
            .put("SerialNumber", "Test1")
            .put("eventType", "bump")
            .put("sensorId", "O2"));
        jList.add(mapper.createObjectNode()
            .put("SerialNumber", "Test1")
            .put("eventType", "cal")
            .put("sensorId", "O2"));
        jList.add(mapper.createObjectNode()
            .put("SerialNumber", "Test2")
            .put("eventType", "bump")
            .put("sensorId", "O2"));
        log.info("Formed JSON Object: {}", jList);

        ReadContext rc = JsonPath.parse(mapper.writeValueAsString(jList));
        String jsonPathExpression = String.format("$.[?(@.%s == '%s' && @.%s =='%s')]", "SerialNumber", "Test1", "eventType", "bump");
        List<Object> result = rc.read(jsonPathExpression);
        log.info("JSON Object: {}", result);
        //log.info("Found JSON List: {}", mapper.convertValue(result, mapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class)).toString());

        /*THis Works perfectly fine */
        //ReadContext rc = JsonPath.parse(jsonArray);
        //String jsonPathExpression = String.format("$.[?(@.%s == '%s')]", fieldName, fieldValue);
        //List<Object> result = rc.read(jsonPathExpression);
        //log.info("Found JSON List: {}", mapper.convertValue(result, mapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class)).toString());
    }

    public String getApplication(String tenantId){
        return subscriptionService.callForTenant(tenantId, ()-> {
            try {
                ApplicationRepresentation res = applicationApi.getByName(appName).get();
               
                log.info("Retrieve Application Id: {}",res);
                log.info("applicationId: {}",res.getId());
                return res.getId();
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                return e.getMessage();
            }
        });
    }

    public String getApplication(){
        return subscriptionService.callForTenant(enterpriseTenantId, ()-> {
            try {
                ApplicationRepresentation res = applicationApi.currentApplication().get();
                log.info("Retrieve Tenant Id: {}",res.toString());
                log.info("applicationId: {}",res.getId());
                return res.getId();
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                return e.getMessage();
            }
        });
    }

    
    public String createMo(){
        return subscriptionService.callForTenant(subscriptionService.getTenant(), ()-> {
            try {
                ManagedObjectRepresentation req = new ManagedObjectRepresentation();
                
                req.setName("Test");
                req.setType("DSXi");
                req.setProperty("c8y_IsDevice", mapper.createObjectNode().asText());
                ManagedObjectRepresentation res = restConnector.post("/inventory/managedObjects", MediaType.APPLICATION_JSON_TYPE, req);
                log.info("Managed Object Response: {}",res.toJSON());
                return res.toString();
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                return e.getMessage();
               
            }
        });
    }

    public String update(){
        return subscriptionService.callForTenant(subscriptionService.getTenant(), ()-> {
            try {
                String res = restConnector.get("/tenant/loginOptions?management=false&tenantId="+subscriptionService.getTenant(), MediaType.APPLICATION_JSON_TYPE, String.class);
                JsonNode r = mapper.readTree(res).get("loginOptions");
                if (r.isArray()){
                    ArrayNode jsonArray = (ArrayNode) r;
                    for(JsonNode n: jsonArray){
                        if(n.get("userManagementSource").asText().equals("INTERNAL")){
                            
                            if(n.get("type").asText().equals("BASIC")){
                                ((ObjectNode) n).put("visibleOnLoginPage", true);
                                log.info("node: {}", n);
                            }else{
                                ((ObjectNode) n).put("visibleOnLoginPage", false);
                                log.info("node: {}", n);
                            }
                            LoginMode lm = mapper.readValue(mapper.writeValueAsString(n), LoginMode.class);
                            log.info("Final Json: {}", lm.toString());
                            LoginMode response = restConnector.put("/tenant/loginOptions/"+n.get("id").asText(), MediaType.APPLICATION_JSON_TYPE, lm);
                            //MediaType.valueOf("application/vnd.com.nsn.cumulocity.authconfig+json") 
                            log.info("response: {}", response.toString());
                        }
                    }
                }
                
                log.info("Managed Object Response: {}",res.toString());
                return res.toString();
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                return e.getMessage();
               
            }
        });
    }

    public String getUser(String tenantId){
        return subscriptionService.callForTenant(tenantId, ()-> {
            try {
                UserRepresentation user = userApi.getUser(tenantId, "vachaspati.diwevedi@softwareag.com");
                log.info("User Response: {}",user.toJSON());

                UserRepresentation res = restConnector.get("/user/"+tenantId+"/users", MediaType.APPLICATION_JSON_TYPE, UserRepresentation.class);
                log.info("User RestConnector Response: {}",res.toString());
                //log.info("Event Status: {}", res);
                return "";
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                return "";
               
            }
        });
    }

   
    public Boolean getTenant(String tenantId){
       return subscriptionService.callForTenant(tenantId, ()-> {
            try {
                OptionRepresentation or = new OptionRepresentation();
                OptionPK op = new OptionPK();
                op.setCategory(category);
                op.setKey(key);
                or = tenantOptionApi.getOption(op);
                log.info("Retrieve Tenant Id: {}",or);
                if(or.getValue().length()>0) 
                    return true;
                return false;
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                return false;
            }
        });
    }

    public void updateTenant(String tenantId){
       subscriptionService.runForTenant(tenantId, ()-> {
            try {
                OptionRepresentation or = new OptionRepresentation();
                or.setCategory(category);
                or.setKey(key);
                or.setValue(tenantId);
                or = tenantOptionApi.save(or);
                log.info("updated successfully: {}",or);
            }catch(Exception e) {
                log.error(errMsg,e.getMessage());
                
            }
        });
    }
 
     public List<String> getAllDeviceNames(String tenantId){
        log.info(String.format("Received TenanttId: {}", tenantId));
        if(tenantId.isEmpty())
            tenantId = subscriptionService.getTenant();
            log.info("Extracted tenantId: {}", tenantId);
        
        return subscriptionService.callForTenant(tenantId, ()-> {
        try {
            List<String> allDeviceNames = new ArrayList<>();
            InventoryFilter inventoryFilter = new InventoryFilter();
            inventoryFilter.byFragmentType(IsDevice.class);

            ManagedObjectCollection managedObjectsByFilter = inventoryApi.getManagedObjectsByFilter(inventoryFilter);
            List<ManagedObjectRepresentation> allObjects = Lists.newArrayList(managedObjectsByFilter.get(5).allPages());

            for (ManagedObjectRepresentation managedObjectRepresentation : allObjects) {
                allDeviceNames.add(managedObjectRepresentation.getName() +", "+managedObjectRepresentation.getSelf());
            }
            log.info("Execution of this $$$$Call For tenant$$$$$$ finished");
            return allDeviceNames;
        } catch (SDKException exception) {
            log.error("Error while loading devices from Cumulocity", exception);
            return null;
        }
    });
        
    }

    public void getAllDeviceNamesRunforTenant(String tenantId){
        log.info(String.format("Received TenanttId: {}", tenantId));
        if(tenantId.isEmpty())
            tenantId = subscriptionService.getTenant();
            log.info("Extracted tenantId: {}", tenantId);
        
        subscriptionService.runForTenant(tenantId, ()-> {
        try {
            List<String> allDeviceNames = new ArrayList<>();
            InventoryFilter inventoryFilter = new InventoryFilter();
            inventoryFilter.byFragmentType(IsDevice.class);

            ManagedObjectCollection managedObjectsByFilter = inventoryApi.getManagedObjectsByFilter(inventoryFilter);
            List<ManagedObjectRepresentation> allObjects = Lists.newArrayList(managedObjectsByFilter.get(2000).allPages());

            for (ManagedObjectRepresentation managedObjectRepresentation : allObjects) {
                allDeviceNames.add(managedObjectRepresentation.getName() +", "+managedObjectRepresentation.getSelf());
            }
            log.info("Execution of this *****run for tenant******* finished");
            
        } catch (SDKException exception) {
            log.error("Error while loading devices from Cumulocity", exception);
            
        }
    });
        
    }

    public void getAllDeviceNamesRunForEachTenants(){
        
        subscriptionService.runForEachTenant(() -> {
        try {
            log.info("Extracted tenantId: {}", subscriptionService.getTenant());
            List<String> allDeviceNames = new ArrayList<>();
            InventoryFilter inventoryFilter = new InventoryFilter();
            inventoryFilter.byFragmentType(IsDevice.class);

            ManagedObjectCollection managedObjectsByFilter = inventoryApi.getManagedObjectsByFilter(inventoryFilter);
            List<ManagedObjectRepresentation> allObjects = Lists.newArrayList(managedObjectsByFilter.get(2000).allPages());

            for (ManagedObjectRepresentation managedObjectRepresentation : allObjects) {
                allDeviceNames.add(managedObjectRepresentation.getName() +", "+managedObjectRepresentation.getSelf());
                log.info("devices:  {}",managedObjectRepresentation.getName() +", "+managedObjectRepresentation.getSelf());
                managedObjectRepresentation.removeProperty("lastUpdated");
                managedObjectRepresentation.setLastUpdatedDateTime(null);
            }
            log.info("Execution of #####run for each tenant##### completed this run finished");

        } catch (SDKException exception) {
            log.error("Error while loading devices from Cumulocity", exception);
        }
    });
        
    }

    public ResponseEntity<String> getTenantOption(String tenantId, String enterpriseTenantId, String category, String key){
        log.info("category: {}", category);
        log.info("key: {}", key);
        log.info("tenantId: {}", tenantId);
        log.info("orgId: {}", enterpriseTenantId);
        // /Boolean.TRUE.equals(validateOrganization(tenantId, key)
        if(true){
            return subscriptionService.callForTenant(subscriptionService.getTenant(), ()-> {
                try {
                    OptionRepresentation or = new OptionRepresentation();
                    OptionPK op = new OptionPK();
                    op.setCategory(category);
                    op.setKey(key);
                    log.info("credentials: {}",op);
                    or = tenantOptionApi.getOption(op);
                    log.info("credentials1: {}",or.getValue());
                    return new ResponseEntity<String>("success", HttpStatus.OK);
                }catch(Exception e) {
                    log.error("failed: {}",e.getMessage());
                    return new ResponseEntity<String>("Access Denied", HttpStatus.FORBIDDEN);
                }
            });
        }
        return new ResponseEntity<String>("Device Not Found", HttpStatus.NOT_FOUND);
    

    }

    public Boolean validateOrganization(String tenantId, String key){
         return subscriptionService.callForTenant(tenantId, ()-> {
            try {
                log.info("tenantId: {}", tenantId);
                ID id = new ID();
                id.setType("c8y_Serial");
                id.setValue(key);
                ExternalIDRepresentation res = identityApi.getExternalId(id);
                log.info("res: {}", res.getManagedObject());
                if (res.getManagedObject()!=null)
                    return true;
                else
                    return false;
            }catch(Exception e) {
                log.error("failed: {}",e.getMessage());
                return false;
            }
         });
    }

    public byte[] downlaodBinary(String tenantId, String id) {
        return subscriptionService.callForTenant(tenantId, ()-> {
            try {
                
                return binariesApi.downloadFile(GId.asGId(id)).readAllBytes();
            }catch(Exception e) {
                log.error("failed: {}",e.getMessage());
                return e.getMessage().getBytes();
            }
         });
    }

    public ResponseEntity<String> createTenantOption(String enterpriseTenantId){
        String category = "inet_DSXI";
        String key = "device";
        String value = "Welcome@12343678";
     
            return subscriptionService.callForTenant(enterpriseTenantId, ()-> {
                try {
                    for (int count=0;count<100000;count++){
                        OptionRepresentation or = new OptionRepresentation();
                        OptionPK op = new OptionPK();
                        or.setCategory(category);
                        or.setKey("credentials."+key+count);
                        or.setValue(value);
                        or = tenantOptionApi.save(or);
                        log.info("count: {}, credentials created: {}",count,or);
                    }
                    return new ResponseEntity<String>(("creation completed"), HttpStatus.OK);
                }catch(Exception e) {
                    log.error("failed: {}",e.getMessage());
                    return new ResponseEntity<String>("Access Denied", HttpStatus.FORBIDDEN);
                }
            });
    }

    public String readFile(String fileClassPath) {
        try{
            StringBuilder response = new StringBuilder();
            ClassPathResource resource = new ClassPathResource(fileClassPath);
            log.info("resource: {}",resource);

            // Read the file
            InputStream inputStream = resource.getInputStream();
           
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            log.info(fileClassPath," : {}",response);
            return response.toString();
           
            
        }catch(Exception e){
            log.error("failed to read file: {}", e.getMessage());
            return e.getMessage();
        }
        
		}

    

 }