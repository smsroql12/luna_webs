package com.example.luna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;

@EnableAsync
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class LunaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LunaApplication.class, args);
    }

}
