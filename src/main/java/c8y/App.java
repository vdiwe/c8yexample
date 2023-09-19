package c8y;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

@MicroserviceApplication
@ComponentScan({"c8y.service","c8y.controller"})
public class App {
    public static void main (String[] args) {
        SpringApplication.run(App.class, args);
    }
}