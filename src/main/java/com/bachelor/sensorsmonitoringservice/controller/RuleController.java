package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.entity.Rule;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.RuleKind;
import com.bachelor.sensorsmonitoringservice.model.enums.ThresholdDirection;
import com.bachelor.sensorsmonitoringservice.repository.RuleRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensors/{sensorId}/rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RuleController {

    private final RuleRepository ruleRepository;
    private final SensorRepository sensorRepository;

    @GetMapping
    public List<Map<String, Object>> getRules(@PathVariable Long sensorId) {
        return ruleRepository.findBySensorId(sensorId).stream()
                .filter(r -> r.getRuleKind() == RuleKind.PERCENT_B)
                .map(this::toMap)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createRule(
            @PathVariable Long sensorId,
            @RequestBody Map<String, Object> body) {

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found"));

        Rule rule = Rule.builder()
                .sensor(sensor)
                .ruleKind(RuleKind.PERCENT_B)
                .level(EventLevel.valueOf((String) body.get("level")))
                .thresholdValue(((Number) body.get("thresholdValue")).doubleValue())
                .thresholdDirection(ThresholdDirection.valueOf((String) body.getOrDefault("direction", "HIGH")))
                .windowSize((Integer) body.getOrDefault("windowSize", 20))
                .enabled((Boolean) body.getOrDefault("enabled", true))
                .build();

        return ResponseEntity.ok(toMap(ruleRepository.save(rule)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(
            @PathVariable Long sensorId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Rule rule = ruleRepository.findById(id)
                .filter(r -> r.getSensor().getId().equals(sensorId))
                .orElseThrow(() -> new IllegalArgumentException("Rule not found"));

        rule.setLevel(EventLevel.valueOf((String) body.get("level")));
        rule.setThresholdValue(((Number) body.get("thresholdValue")).doubleValue());
        rule.setThresholdDirection(ThresholdDirection.valueOf((String) body.getOrDefault("direction", "HIGH")));
        rule.setWindowSize((Integer) body.getOrDefault("windowSize", 20));
        rule.setEnabled((Boolean) body.getOrDefault("enabled", true));

        return ResponseEntity.ok(toMap(ruleRepository.save(rule)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long sensorId, @PathVariable Long id) {
        ruleRepository.findById(id)
                .filter(r -> r.getSensor().getId().equals(sensorId))
                .ifPresent(ruleRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(Rule r) {
        return Map.of(
                "id",             r.getId(),
                "level",          r.getLevel().name(),
                "thresholdValue", r.getThresholdValue(),
                "direction",      r.getThresholdDirection().name(),
                "windowSize",     r.getWindowSize(),
                "enabled",        r.getEnabled()
        );
    }
}
