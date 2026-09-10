package com.streamx.addecisioning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class AdDecisioningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdDecisioningServiceApplication.class, args);
    }
}
