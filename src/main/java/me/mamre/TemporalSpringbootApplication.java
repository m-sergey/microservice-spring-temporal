package me.mamre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
@Slf4j
public class TemporalSpringbootApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemporalSpringbootApplication.class, args).start();
        log.info("TemporalSpringbootApplication was started");
    }
}
