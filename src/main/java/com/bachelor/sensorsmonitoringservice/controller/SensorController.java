package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.dto.SensorDto;
import com.bachelor.sensorsmonitoringservice.model.dto.SensorThresholdsRequest;
import com.bachelor.sensorsmonitoringservice.model.entity.Rule;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.RuleKind;
import com.bachelor.sensorsmonitoringservice.model.enums.ThresholdDirection;
import com.bachelor.sensorsmonitoringservice.repository.RuleRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import com.bachelor.sensorsmonitoringservice.service.RulesEngineService;
import com.bachelor.sensorsmonitoringservice.service.SseTelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SensorController {
    
    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    private final RuleRepository ruleRepository;
    private final SseTelemetryService sseTelemetryService;
    private final RulesEngineService rulesEngineService;
    
    @GetMapping
    public List<SensorDto> getAllSensors() {
        return sensorRepository.findAll().stream()
                .sorted(Comparator.comparing(Sensor::getName))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private SensorDto convertToDto(Sensor sensor) {
        return SensorDto.builder()
                .id(sensor.getId())
                .sensorKey(sensor.getSensorKey())
                .name(sensor.getName())
                .type(sensor.getType().name())
                .unit(sensor.getUnit())
                .deviceId(sensor.getDevice() != null ? sensor.getDevice().getId() : null)
                .currentStatus(sensor.getCurrentStatus())
                .isActive(sensor.getIsActive())
                .lastValue(sensor.getLastValue())
                .lastUpdated(sensor.getLastUpdated())
                .build();
    }
    
    @PutMapping("/{id}/thresholds")
    public ResponseEntity<Void> saveThresholds(@PathVariable Long id,
                                               @RequestBody SensorThresholdsRequest req) {
        return sensorRepository.findById(id).map(sensor -> {
            ruleRepository.findBySensorId(id).stream()
                    .filter(r -> r.getRuleKind() == RuleKind.THRESHOLD)
                    .forEach(ruleRepository::delete);

            saveRule(sensor, req.getLowCritical(),  ThresholdDirection.LOW,    EventLevel.EMERGENCY);
            saveRule(sensor, req.getLowWarning(),   ThresholdDirection.LOW,    EventLevel.WARNING);
            saveRule(sensor, req.getNormal(),       ThresholdDirection.NORMAL, EventLevel.WARNING);
            saveRule(sensor, req.getHighWarning(),  ThresholdDirection.HIGH,   EventLevel.WARNING);
            saveRule(sensor, req.getHighCritical(), ThresholdDirection.HIGH,   EventLevel.EMERGENCY);
            ruleRepository.flush();

            // Re-evaluate status immediately using current lastValue
            if (sensor.getLastValue() != null) {
                rulesEngineService.evaluateRules(sensor.getId(), sensor.getLastValue(), java.time.Instant.now());
            }

            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private void saveRule(Sensor sensor, Double value, ThresholdDirection dir, EventLevel level) {
        if (value == null) return;
        ruleRepository.save(Rule.builder()
                .sensor(sensor)
                .ruleKind(RuleKind.THRESHOLD)
                .thresholdDirection(dir)
                .level(level)
                .thresholdValue(value)
                .enabled(true)
                .build());
    }

    @GetMapping("/{id}/thresholds")
    public ResponseEntity<SensorThresholdsRequest> getThresholds(@PathVariable Long id) {
        List<Rule> rules = ruleRepository.findBySensorId(id).stream()
                .filter(r -> r.getRuleKind() == RuleKind.THRESHOLD)
                .collect(Collectors.toList());

        SensorThresholdsRequest dto = new SensorThresholdsRequest();
        for (Rule r : rules) {
            ThresholdDirection dir = r.getThresholdDirection() != null ? r.getThresholdDirection() : ThresholdDirection.HIGH;
            if (dir == ThresholdDirection.LOW && r.getLevel() == EventLevel.EMERGENCY)  dto.setLowCritical(r.getThresholdValue());
            if (dir == ThresholdDirection.LOW && r.getLevel() == EventLevel.WARNING)    dto.setLowWarning(r.getThresholdValue());
            if (dir == ThresholdDirection.NORMAL)                                       dto.setNormal(r.getThresholdValue());
            if (dir == ThresholdDirection.HIGH && r.getLevel() == EventLevel.WARNING)   dto.setHighWarning(r.getThresholdValue());
            if (dir == ThresholdDirection.HIGH && r.getLevel() == EventLevel.EMERGENCY) dto.setHighCritical(r.getThresholdValue());
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorDto> updateSensor(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> body) {
        return sensorRepository.findById(id).map(sensor -> {
            if (body.containsKey("name")) sensor.setName((String) body.get("name"));
            sensorRepository.save(sensor);
            return ResponseEntity.ok(convertToDto(sensor));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorDto> getSensor(@PathVariable Long id) {
        return sensorRepository.findById(id)
                .map(s -> ResponseEntity.ok(convertToDto(s)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSensor(@PathVariable Long id) {
        return sseTelemetryService.subscribe(id);
    }

    @GetMapping("/{id}/telemetry")
    public Page<Map<String, Object>> getSensorTelemetry(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("ts").descending());
        Page<Telemetry> raw = telemetryRepository.findBySensorId(id, pageRequest);
        List<Map<String, Object>> flat = raw.getContent().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "sensorId", id,
                        "value", t.getValue(),
                        "timestamp", t.getTs().toString(),
                        "quality", t.getQuality()
                ))
                .collect(Collectors.toList());
        return new PageImpl<>(flat, pageRequest, raw.getTotalElements());
    }

    @GetMapping("/{id}/telemetry/latest")
    public ResponseEntity<Map<String, Object>> getLatestTelemetry(@PathVariable Long id) {
        return telemetryRepository.findTopBySensorIdOrderByTsDesc(id)
                .map(t -> ResponseEntity.ok(Map.<String, Object>of(
                        "id", t.getId(),
                        "sensorId", id,
                        "value", t.getValue(),
                        "timestamp", t.getTs().toString(),
                        "quality", t.getQuality()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}