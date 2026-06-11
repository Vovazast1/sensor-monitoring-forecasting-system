package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.dto.MLPredictRequest;
import com.bachelor.sensorsmonitoringservice.model.dto.MLPredictResponse;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import com.bachelor.sensorsmonitoringservice.service.MLMonitoringService;
import com.bachelor.sensorsmonitoringservice.service.MLServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MLPredictionController {
    
    private final MLServiceClient mlServiceClient;
    private final MLMonitoringService mlMonitoringService;
    private final TelemetryRepository telemetryRepository;
    private final SensorRepository sensorRepository;
    
    @PostMapping("/predict")
    public Mono<MLPredictResponse> predict(@RequestBody MLPredictRequest request) {
        return mlServiceClient.predict(request);
    }
    
    @GetMapping("/predict/sensor/{sensorId}")
    public Mono<ResponseEntity<MLPredictResponse>> predictForSensor(@PathVariable Long sensorId) {
        return Mono.fromCallable(() -> sensorRepository.findById(sensorId))
                .flatMap(optionalSensor -> {
                    if (optionalSensor.isEmpty()) {
                        return Mono.just(ResponseEntity.notFound().<MLPredictResponse>build());
                    }

                    var sensor = optionalSensor.get();
                    List<Telemetry> telemetryList = telemetryRepository.findLatestBySensorId(sensorId, 120);

                    if (telemetryList.size() < 120) {
                        return Mono.just(ResponseEntity.badRequest().<MLPredictResponse>build());
                    }

                    List<Double> values = telemetryList.stream()
                            .map(Telemetry::getValue)
                            .collect(Collectors.toList());

                    MLPredictRequest request = MLPredictRequest.builder()
                            .sensorId(sensor.getSensorKey())
                            .values(values)
                            .build();

                    return mlServiceClient.predict(request)
                            .map(ResponseEntity::ok)
                            .defaultIfEmpty(ResponseEntity.noContent().build());
                });
    }
    
    @GetMapping("/predictions")
    public ResponseEntity<List<MLPredictResponse>> getAllPredictions() {
        return ResponseEntity.ok(mlMonitoringService.getCachedPredictions());
    }
}
