package com.streamx.transcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class TranscodeWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranscodeWorkerApplication.class, args);
    }
}
