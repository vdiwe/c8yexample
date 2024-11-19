package c8y.controller;

import java.util.List;

import com.cumulocity.sdk.client.Platform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import c8y.model.TenantDetails;
import c8y.service.ExampleService;
import c8y.service.GenerateToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * This is an example controller. This should be removed for your real project!
 * 
 * @author APES
 *
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ExampleController {
	

	@Value("${C8Y.bootstrap.tenant}")
	private String enterpriseTenantId;

	@Qualifier("userPlatform")
	private Platform userPlatform;
	
	@Autowired
	private ExampleService deviceService;

	@Autowired
	private GenerateToken generateToken;
	
	@Operation(summary = "Get All devices", description= "THis API will list down all the devices from tenant")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Not found")})
	@PreAuthorize("hasRole('ROLE_HELLO-_READ')")
	@GetMapping(path = "/devices", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getAllDeviceNames() {
		String tenantId="";
        List<String> response = deviceService.getAllDeviceNames(tenantId);
		return new ResponseEntity<List<String>>(response, HttpStatus.OK);
    }
	@PreAuthorize("hasRole('ROLE_HELLO-_READ')")	
	@GetMapping(path = "/tenantOption/{tenantId}/{category}/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTenantOption(@PathVariable String tenantId, @PathVariable String category, @PathVariable String key) {
  		return deviceService.getTenantOption(tenantId, enterpriseTenantId, category, key);
		
    }
	@PreAuthorize("hasRole('ROLE_HELLO-_READ')")
	@GetMapping(path = "/downlaodBinary/{tenantId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getBinary(@PathVariable String tenantId, @PathVariable String id) {
        byte[] response = deviceService.downlaodBinary(tenantId, id);
		return new ResponseEntity<byte[]>(response, HttpStatus.OK);
    }
	@PreAuthorize("hasRole('ROLE_HELLO-_CREATE')")	
	@PostMapping(path = "/mo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> mocreate() {
		
  		return new ResponseEntity<String>(deviceService.update(), HttpStatus.OK);
		
    }

	@PreAuthorize("hasRole('ROLE_HELLO-_READ')")	
	@GetMapping(path = "/mo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> moGet() {
		
  		return new ResponseEntity<>("right access", HttpStatus.OK);
		
    }


	@GetMapping(path = "/option", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createOptions() {
         return deviceService.createTenantOption("t1228040");
    }

	@GetMapping(path="/getOption/{category}/{key}")
	public ResponseEntity<String> getOption(@PathVariable String category, @PathVariable String key){
			return new ResponseEntity<String>(deviceService.getTenantOption("t2628005","t1228040", category, key).toString(), HttpStatus.OK);
	}

	@GetMapping(path="/test")
	public ResponseEntity<List<String>> test(){
		log.info("Starting RUN TENANT");
		deviceService.getAllDeviceNamesRunforTenant(enterpriseTenantId);
		log.info("Finished RUN TENANT");
		log.info("Starting RUN FOR EACH TENANT");
		deviceService.getAllDeviceNamesRunForEachTenants();
		log.info("FINISHED RUN FOR EACH TENANT");
		return new ResponseEntity<>(deviceService.getAllDeviceNames(""), HttpStatus.OK);
	}

	@GetMapping(path="/test/{tenantId}")
	public ResponseEntity<List<String>> test1(@PathVariable String tenantId){
			return new ResponseEntity<>(deviceService.getAllDeviceNames(tenantId), HttpStatus.OK);
	}
	@GetMapping(path="/app")
	public ResponseEntity<String> test1(){
			return new ResponseEntity<>(deviceService.getApplication(), HttpStatus.OK);
	}

	@Scheduled(cron = "1 * * * * *")
	public void scheduleTaskUsingCronExpression() {
	    //deviceService.getAllDeviceNames(enterpriseTenantId);
		//deviceService.getAllDeviceNamesRunForEachTenants();

	}

	@GetMapping(path="/readFile/{filename}")
	public ResponseEntity<String> readFile(@PathVariable String filename){
			return new ResponseEntity<>(deviceService.readFile(filename), HttpStatus.OK);
	}

	@PostMapping(path = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TenantDetails> token(@RequestBody List<TenantDetails> tenantDetails) {
		log.info("tenant Details: {}", tenantDetails);
		return generateToken.createToken();
	}
	
	
}
