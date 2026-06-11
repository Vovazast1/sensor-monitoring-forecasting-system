package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.entity.Candle;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.repository.CandleRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleAggregationService {

    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    private final CandleRepository candleRepository;
    private final RulesEngineService rulesEngineService;

    @Scheduled(cron = "*/15 * * * * *")
    @Transactional
    public void aggregateLastMinute() {
        Instant minuteEnd = Instant.now().minusMillis(Instant.now().toEpochMilli() % 15000);
        Instant minuteStart = minuteEnd.minus(15, ChronoUnit.SECONDS);

        sensorRepository.findAll().forEach(sensor -> buildCandle(sensor, minuteStart, minuteEnd));
    }

    private void buildCandle(Sensor sensor, Instant from, Instant to) {
        if (candleRepository.existsBySensorIdAndTs(sensor.getId(), from)) return;

        List<Telemetry> data = telemetryRepository.findBySensorIdAndTsBetween(sensor.getId(), from, to);
        if (data.isEmpty()) return;

        List<Telemetry> sorted = data.stream()
                .sorted(Comparator.comparing(Telemetry::getTs))
                .toList();

        Candle candle = Candle.builder()
                .sensor(sensor)
                .ts(from)
                .open(sorted.getFirst().getValue())
                .close(sorted.getLast().getValue())
                .high(sorted.stream().mapToDouble(Telemetry::getValue).max().orElseThrow())
                .low(sorted.stream().mapToDouble(Telemetry::getValue).min().orElseThrow())
                .build();

        candleRepository.save(candle);
        log.debug("Candle saved: sensor={} ts={}", sensor.getId(), from);
        rulesEngineService.evaluatePercentBRules(sensor.getId());
    }
}
