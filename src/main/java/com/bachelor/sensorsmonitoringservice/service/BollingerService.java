package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.entity.Candle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BollingerService {

    public record BollingerPoint(long ts, double ma, double upper, double lower) {}

    public List<BollingerPoint> calculate(List<Candle> candles, int window) {
        List<BollingerPoint> result = new ArrayList<>();

        for (int i = window - 1; i < candles.size(); i++) {
            List<Candle> slice = candles.subList(i - window + 1, i + 1);

            double ma = slice.stream().mapToDouble(Candle::getClose).average().orElse(0);
            double variance = slice.stream()
                    .mapToDouble(c -> Math.pow(c.getClose() - ma, 2))
                    .average().orElse(0);
            double std = Math.sqrt(variance);

            result.add(new BollingerPoint(
                    candles.get(i).getTs().toEpochMilli(),
                    round(ma),
                    round(ma + 2 * std),
                    round(ma - 2 * std)
            ));
        }
        return result;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
