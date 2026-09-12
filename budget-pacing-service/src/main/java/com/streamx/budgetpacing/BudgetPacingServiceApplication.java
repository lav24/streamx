package com.streamx.budgetpacing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class BudgetPacingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetPacingServiceApplication.class, args);
    }
}
