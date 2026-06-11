package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Random;

@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TelemetryController {
    
    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    private final Random random = new Random();
    
    @PostMapping("/generate")
    public ResponseEntity<String> generateRandomData() {
        for (Sensor sensor : sensorRepository.findAll()) {
            double value = getBaseValue(sensor) + (random.nextDouble() - 0.5) * 10;

            Telemetry telemetry = new Telemetry();
            telemetry.setSensor(sensor);
            telemetry.setTs(Instant.now());
            telemetry.setValue(value);
            telemetry.setQuality(1);
            telemetryRepository.save(telemetry);

            sensor.setLastValue(value);
            sensor.setLastUpdatedAt(Instant.now());
            sensorRepository.save(sensor);
        }
        return ResponseEntity.ok("Generated random data for all sensors");
    }
    
    private double getBaseValue(Sensor sensor) {
        return switch (sensor.getType()) {
            case TEMPERATURE -> 25.0;
            case HUMIDITY -> 50.0;
            case CURRENT -> 8.0;
            case LIGHT -> 200.0;
            default -> 50.0;
        };
    }
}
