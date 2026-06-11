package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.entity.Rule;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.RuleKind;
import com.bachelor.sensorsmonitoringservice.model.enums.ThresholdDirection;
import com.bachelor.sensorsmonitoringservice.repository.RuleRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultRulesService implements CommandLineRunner {

    private final SensorRepository sensorRepository;
    private final RuleRepository ruleRepository;

    // lowCritical, lowWarning, normal, highWarning, highCritical
    private static final Map<String, double[]> DEFAULTS = Map.of(
            "TEMPERATURE", new double[]{10, 17, 25, 32, 40},
            "HUMIDITY",    new double[]{10, 25, 40, 55, 70},
            "LIGHT",       new double[]{10, 20, 100, 200, 400}
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding default threshold rules...");
        sensorRepository.findAll().forEach(this::seedRulesForSensor);
    }

    @Transactional
    public void seedRulesForSensor(Sensor sensor) {
        boolean hasThresholds = ruleRepository.findBySensorId(sensor.getId()).stream()
                    .anyMatch(r -> r.getRuleKind() == RuleKind.THRESHOLD);
        if (hasThresholds)
            return;

        String type = sensor.getType().name();
        double[] d = DEFAULTS.get(type);
        log.info("Sensor {} type={} hasDefaults={}", sensor.getSensorKey(), type, d != null);
        if (d == null)
            return;

        saveRule(sensor, d[0], ThresholdDirection.LOW,    EventLevel.EMERGENCY);
        saveRule(sensor, d[1], ThresholdDirection.LOW,    EventLevel.WARNING);
        saveRule(sensor, d[2], ThresholdDirection.NORMAL, EventLevel.WARNING);
        saveRule(sensor, d[3], ThresholdDirection.HIGH,   EventLevel.WARNING);
        saveRule(sensor, d[4], ThresholdDirection.HIGH,   EventLevel.EMERGENCY);

        log.info("Seeded default threshold rules for sensor {} ({})", sensor.getSensorKey(), type);
    }

    private void saveRule(Sensor sensor, double value, ThresholdDirection dir, EventLevel level) {
        ruleRepository.save(Rule.builder()
                .sensor(sensor)
                .ruleKind(RuleKind.THRESHOLD)
                .thresholdDirection(dir)
                .level(level)
                .thresholdValue(value)
                .enabled(true)
                .build());
    }
}
