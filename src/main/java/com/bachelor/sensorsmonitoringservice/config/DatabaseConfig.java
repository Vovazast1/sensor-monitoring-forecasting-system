package com.bachelor.sensorsmonitoringservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.bachelor.sensorsmonitoringservice.repository")
@EnableTransactionManagement
public class DatabaseConfig {
}