package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.dto.MLCollectRequest;
import com.bachelor.sensorsmonitoringservice.model.dto.MLPredictRequest;
import com.bachelor.sensorsmonitoringservice.model.dto.MLPredictResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class MLServiceClient {
    
    private final WebClient webClient;
    private final int timeout;
    private final int maxAttempts;
    private final long backoff;
    
    public MLServiceClient(
            @Value("${ml.service.url}") String mlServiceUrl,
            @Value("${ml.service.timeout}") int timeout,
            @Value("${ml.service.retry.max-attempts}") int maxAttempts,
            @Value("${ml.service.retry.backoff}") long backoff) {
        
        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
        
        log.info("ML Service Client initialized with URL: {}", mlServiceUrl);
    }
    
    public Mono<Void> collect(MLCollectRequest request) {
        return webClient.post()
                .uri("/collect")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(error -> {
                    log.warn("ML collect failed for sensor {}: {}", request.getSensorId(), error.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<MLPredictResponse> predict(MLPredictRequest request) {
        log.debug("Sending prediction request for sensor: {}", request.getSensorId());
        
        return webClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MLPredictResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.fixedDelay(maxAttempts, Duration.ofMillis(backoff)))
                .doOnSuccess(response -> log.debug("Prediction received for sensor {}: gru={}, arima={}",
                        response.getSensorId(), response.getGruForecast(), response.getArimaForecast()))
                .onErrorResume(error -> {
                    log.warn("ML prediction unavailable for sensor {}: {}", 
                            request.getSensorId(), error.getMessage());
                    return Mono.empty();
                });
    }
}
