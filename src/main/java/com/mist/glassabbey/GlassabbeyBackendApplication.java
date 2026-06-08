package com.mist.glassabbey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GlassabbeyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlassabbeyBackendApplication.class, args);
    }

}
