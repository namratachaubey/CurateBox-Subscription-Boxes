package com.example.subscriptionbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubscriptionBoxApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionBoxApplication.class, args);
    }
}
