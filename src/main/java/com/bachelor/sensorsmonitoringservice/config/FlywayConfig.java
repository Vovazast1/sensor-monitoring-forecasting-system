package com.bachelor.sensorsmonitoringservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlywayConfig {
    
    private final DataSource dataSource;
    
    @PostConstruct
    public void migrate() {
        log.info("Cleaning databllkase and running migrations...");
        var flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(dataSource)
                .cleanDisabled(false)
                .load();
        flyway.clean();
        flyway.migrate();
    }
}
