package c8y;


import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;


@MicroserviceApplication
@EnableScheduling
//@ComponentScan({"c8y.service","c8y.controller", "c8y"})
public class App {

    public static void main (String[] args) {
        SpringApplication.run(App.class, args);       
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}