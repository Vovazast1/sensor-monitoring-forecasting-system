package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.entity.Candle;
import com.bachelor.sensorsmonitoringservice.repository.CandleRepository;
import com.bachelor.sensorsmonitoringservice.service.BollingerService;
import com.bachelor.sensorsmonitoringservice.service.IndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChartController {

    private final CandleRepository candleRepository;
    private final BollingerService bollingerService;
    private final IndicatorService indicatorService;

    @GetMapping("/{id}/chart")
    public ResponseEntity<Map<String, Object>> getChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1h") String range,
            @RequestParam(defaultValue = "20") int window) {

        Instant from = switch (range) {
            case "30m" -> Instant.now().minus(30, ChronoUnit.MINUTES);
            case "3h"  -> Instant.now().minus(3, ChronoUnit.HOURS);
            case "6h"  -> Instant.now().minus(6, ChronoUnit.HOURS);
            case "24h" -> Instant.now().minus(24, ChronoUnit.HOURS);
            default    -> Instant.now().minus(1, ChronoUnit.HOURS);
        };

        List<Candle> candles = candleRepository.findBySensorIdSince(id, from);

        List<Map<String, Object>> candleData = candles.stream()
                .map(c -> Map.<String, Object>of(
                        "ts",    c.getTs().toEpochMilli(),
                        "open",  c.getOpen(),
                        "high",  c.getHigh(),
                        "low",   c.getLow(),
                        "close", c.getClose()
                ))
                .toList();

        List<BollingerService.BollingerPoint> bollinger =
                candles.size() >= window ? bollingerService.calculate(candles, window) : List.of();

        List<IndicatorService.PercentBPoint> percentB =
                bollinger.isEmpty() ? List.of() : indicatorService.calculatePercentB(candles, bollinger);

        return ResponseEntity.ok(Map.of(
                "candles",   candleData,
                "bollinger", bollinger,
                "percentB",  percentB
        ));
    }
}
