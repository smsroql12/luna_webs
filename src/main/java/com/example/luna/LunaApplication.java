package com.example.luna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@EnableAsync
@EnableScheduling
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class LunaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LunaApplication.class, args);
    }

}
