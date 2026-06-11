package com.bachelor.sensorsmonitoringservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SensorsMonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorsMonitoringServiceApplication.class, args);
	}

}
