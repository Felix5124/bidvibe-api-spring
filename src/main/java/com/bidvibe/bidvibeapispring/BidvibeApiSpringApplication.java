package com.bidvibe.bidvibeapispring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidvibeApiSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidvibeApiSpringApplication.class, args);
    }

}
