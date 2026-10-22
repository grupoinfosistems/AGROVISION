package com.agrovision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class AgrovisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrovisionApplication.class, args);
    }
}
