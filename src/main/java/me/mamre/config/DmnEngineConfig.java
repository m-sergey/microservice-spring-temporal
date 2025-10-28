package me.mamre.config;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class DmnEngineConfig {
    @Bean
    public DmnEngine dmnEngine() {
        return DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
    }
}