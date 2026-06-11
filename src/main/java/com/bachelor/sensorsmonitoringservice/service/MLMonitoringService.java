package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.dto.MLCollectRequest;
import com.bachelor.sensorsmonitoringservice.model.dto.MLPredictRequest;
import com.bachelor.sensorsmonitoringservice.model.dto.MLPredictResponse;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLMonitoringService {
    
    private final MLServiceClient mlServiceClient;
    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, MLPredictResponse> predictionCache = new ConcurrentHashMap<>();

    public List<MLPredictResponse> getCachedPredictions() {
        return new ArrayList<>(predictionCache.values());
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    @Scheduled(fixedDelay = 10000)
    public void checkSensors() {
        List<Sensor> sensors = sensorRepository.findAll();
        
        for (Sensor sensor : sensors) {
            try {
                checkSensor(sensor);
            } catch (Exception e) {
                log.error("Error checking sensor {}: {}", sensor.getSensorKey(), e.getMessage());
            }
        }
    }
    
    private void checkSensor(Sensor sensor) {
        List<Telemetry> telemetryList = telemetryRepository.findLatestBySensorId(sensor.getId(), 120);

        if (telemetryList.size() < 120) {
            log.debug("Insufficient data for sensor {}: {} values", sensor.getSensorKey(), telemetryList.size());
            return;
        }

        List<Double> values = telemetryList.stream()
                .map(Telemetry::getValue)
                .collect(Collectors.toList());

        MLPredictRequest request = MLPredictRequest.builder()
                .sensorId(sensor.getSensorKey())
                .values(values)
                .build();

        mlServiceClient.predict(request)
                .subscribe(
                        response -> {
                            predictionCache.put(sensor.getSensorKey(), response);
                            handlePredictionResponse(sensor, response);
                            scheduleCollect(sensor, values);
                        },
                        error -> log.error("ML prediction failed for sensor {}: {}",
                                sensor.getSensorKey(), error.getMessage())
                );
    }

    private void scheduleCollect(Sensor sensor, List<Double> values) {
        scheduler.schedule(() -> {
            telemetryRepository.findTopBySensorIdOrderByTsDesc(sensor.getId())
                    .ifPresent(latest -> {
                        MLCollectRequest collect = MLCollectRequest.builder()
                                .sensorId(sensor.getSensorKey())
                                .values(values)
                                .actualValue(latest.getValue())
                                .build();
                        mlServiceClient.collect(collect).subscribe();
                        log.debug("Collected sample for {}: actual={}",
                                sensor.getSensorKey(), latest.getValue());
                    });
        }, 10, TimeUnit.SECONDS);
    }
    
    private void handlePredictionResponse(Sensor sensor, MLPredictResponse response) {
        log.info("ML Prediction for sensor {}: gru_forecast={}, arima_forecast={}",
                sensor.getSensorKey(), response.getGruForecast(), response.getArimaForecast());
    }
}
