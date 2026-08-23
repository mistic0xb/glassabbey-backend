package com.mist.glassabbey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class GlassabbeyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlassabbeyBackendApplication.class, args);
    }

}
