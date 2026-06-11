package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.events.SensorStatusChangedEvent;
import com.bachelor.sensorsmonitoringservice.model.entity.Candle;
import com.bachelor.sensorsmonitoringservice.model.entity.Rule;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.RuleKind;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorStatus;
import com.bachelor.sensorsmonitoringservice.model.enums.ThresholdDirection;
import com.bachelor.sensorsmonitoringservice.repository.CandleRepository;
import com.bachelor.sensorsmonitoringservice.repository.RuleRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RulesEngineService {
    
    private final RuleRepository ruleRepository;
    private final TelemetryRepository telemetryRepository;
    private final SensorRepository sensorRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SseTelemetryService sseTelemetryService;
    private final CandleRepository candleRepository;
    private final BollingerService bollingerService;
    private final IndicatorService indicatorService;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void evaluateRules(Long sensorId, double value, Instant ts) {
        Sensor sensor = sensorRepository.findById(sensorId).orElseThrow();
        List<Rule> rules = ruleRepository.findBySensorId(sensorId);

        SensorStatus newStatus = classifyValue(value, rules);

        if (newStatus != sensor.getStatus()) {
            updateSensorStatus(sensor, newStatus, value);
        }

        // Also run TREND rules on top
        rules.stream()
                .filter(r -> r.getEnabled() && r.getRuleKind() == RuleKind.TREND)
                .forEach(r -> {
                    SensorStatus trendStatus = evaluateTrendRule(r, sensor);
                    if (trendStatus != null && trendStatus != sensor.getStatus()) {
                        updateSensorStatus(sensor, trendStatus, value);
                    }
                });

        sseTelemetryService.push(sensorId, value, ts.toString(), sensor.getStatus().name());
    }

    /**
     * Classify a value into a SensorStatus zone based on the sensor's threshold rules.
     * Priority (outermost wins): LOW_CRITICAL > LOW_WARNING > HIGH_CRITICAL > HIGH_WARNING > NORMAL
     */
    private SensorStatus classifyValue(double value, List<Rule> rules) {
        Double lowCritical  = null, lowWarning  = null;
        Double highWarning  = null, highCritical = null;

        for (Rule r : rules) {
            if (!r.getEnabled() || r.getRuleKind() != RuleKind.THRESHOLD || r.getThresholdValue() == null) continue;
            ThresholdDirection dir = r.getThresholdDirection() != null ? r.getThresholdDirection() : ThresholdDirection.HIGH;
            if (dir == ThresholdDirection.LOW  && r.getLevel() == EventLevel.EMERGENCY) lowCritical  = r.getThresholdValue();
            if (dir == ThresholdDirection.LOW  && r.getLevel() == EventLevel.WARNING)   lowWarning   = r.getThresholdValue();
            if (dir == ThresholdDirection.HIGH && r.getLevel() == EventLevel.WARNING)   highWarning  = r.getThresholdValue();
            if (dir == ThresholdDirection.HIGH && r.getLevel() == EventLevel.EMERGENCY) highCritical = r.getThresholdValue();
        }

        if (lowCritical  != null && value <= lowCritical)  return SensorStatus.LOW_CRITICAL;
        if (lowWarning   != null && value <= lowWarning)   return SensorStatus.LOW_WARNING;
        if (highCritical != null && value >= highCritical) return SensorStatus.HIGH_CRITICAL;
        if (highWarning  != null && value >= highWarning)  return SensorStatus.HIGH_WARNING;
        log.debug("classifyValue: v={} lc={} lw={} hw={} hc={} → NORMAL", value, lowCritical, lowWarning, highWarning, highCritical);
        return SensorStatus.NORMAL;
    }
    
    private SensorStatus evaluateTrendRule(Rule rule, Sensor sensor) {
        // Get last N values for trend analysis
        List<Telemetry> recentData = telemetryRepository.findLatestBySensorId(
            sensor.getId(), rule.getWindowSize());
        
        if (recentData.size() < rule.getWindowSize()) {
            return null; // Not enough data
        }

        int increases = 0;
        for (int i = 1; i < recentData.size(); i++) {
            if (recentData.get(i-1).getValue() < recentData.get(i).getValue()) {
                increases++;
            }
        }
        
        if (increases >= rule.getMinIncreases()) {
            log.info("Trend detected: {} increases out of {} values for sensor {}", increases, rule.getWindowSize(), sensor.getSensorKey());
            return SensorStatus.HIGH_WARNING;
        }
        
        return null;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void evaluatePercentBRules(Long sensorId) {
        List<Rule> rules = ruleRepository.findBySensorId(sensorId).stream()
                .filter(r -> r.getEnabled() && r.getRuleKind() == RuleKind.PERCENT_B)
                .toList();
        if (rules.isEmpty()) return;

        Sensor sensor = sensorRepository.findById(sensorId).orElseThrow();

        for (Rule rule : rules) {
            int window = rule.getWindowSize();
            List<Candle> candles = candleRepository.findLastN(sensorId, window);
            if (candles.size() < window) continue;

            // findLastN returns DESC — reverse to chronological order
            candles = candles.reversed();

            List<BollingerService.BollingerPoint> bollinger = bollingerService.calculate(candles, window);
            if (bollinger.isEmpty()) continue;

            List<IndicatorService.PercentBPoint> percentBPoints =
                    indicatorService.calculatePercentB(candles, bollinger);
            if (percentBPoints.isEmpty()) continue;

            double latestPercentB = percentBPoints.getLast().percentB();
            ThresholdDirection dir = rule.getThresholdDirection() != null
                    ? rule.getThresholdDirection() : ThresholdDirection.HIGH;

            boolean triggered = (dir == ThresholdDirection.HIGH && latestPercentB >= rule.getThresholdValue())
                    || (dir == ThresholdDirection.LOW && latestPercentB <= rule.getThresholdValue());

            if (triggered) {
                SensorStatus target = rule.getLevel() == EventLevel.EMERGENCY
                        ? SensorStatus.HIGH_CRITICAL : SensorStatus.HIGH_WARNING;
                if (target != sensor.getStatus()) {
                    log.info("%%B rule triggered: sensor={} %%B={} {} threshold={} → {}",
                            sensorId, latestPercentB, dir, rule.getThresholdValue(), target);
                    updateSensorStatus(sensor, target, latestPercentB);
                }
            }
        }
    }

    private void updateSensorStatus(Sensor sensor, SensorStatus newStatus, double value) {
        SensorStatus previousStatus = sensor.getStatus();
        sensor.setStatus(newStatus);
        sensorRepository.save(sensor);

        String message = String.format("Status changed to %s (value=%.2f)", newStatus.getDescription(), value);
        String deviceName = sensor.getDevice().getName(); // resolve while session is open
        SensorStatusChangedEvent event = new SensorStatusChangedEvent(
                this, sensor, previousStatus, newStatus,
                newStatus.isCritical() ? EventLevel.EMERGENCY : EventLevel.WARNING,
                message, "THRESHOLD", deviceName);
        eventPublisher.publishEvent(event);
    }
}