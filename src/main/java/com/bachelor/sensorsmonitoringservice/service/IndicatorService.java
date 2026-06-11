package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.entity.Candle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndicatorService {

    public record RsiPoint(long ts, double rsi) {}
    public record MacdPoint(long ts, double macd, double signal, double histogram) {}
    public record PercentBPoint(long ts, double percentB) {}
    public record EmaPoint(long ts, double ema) {}
    public record RocPoint(long ts, double roc) {}
    public record ZScorePoint(long ts, double zScore) {}

    // RSI-14
    public List<RsiPoint> calculateRsi(List<Candle> candles, int period) {
        List<RsiPoint> result = new ArrayList<>();
        if (candles.size() < period + 1) return result;

        double avgGain = 0, avgLoss = 0;
        for (int i = 1; i <= period; i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            if (change > 0) avgGain += change; else avgLoss -= change;
        }
        avgGain /= period;
        avgLoss /= period;

        result.add(new RsiPoint(candles.get(period).getTs().toEpochMilli(), rsi(avgGain, avgLoss)));

        for (int i = period + 1; i < candles.size(); i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            double gain = change > 0 ? change : 0;
            double loss = change < 0 ? -change : 0;
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;
            result.add(new RsiPoint(candles.get(i).getTs().toEpochMilli(), rsi(avgGain, avgLoss)));
        }
        return result;
    }

    private double rsi(double avgGain, double avgLoss) {
        if (avgLoss == 0) return 100;
        return round(100 - 100 / (1 + avgGain / avgLoss));
    }

    // MACD (12, 26, 9)
    public List<MacdPoint> calculateMacd(List<Candle> candles, int fast, int slow, int signal) {
        List<MacdPoint> result = new ArrayList<>();
        if (candles.size() < slow + signal) return result;

        List<Double> closes = candles.stream().map(Candle::getClose).toList();
        List<Double> emaFast = ema(closes, fast);
        List<Double> emaSlow = ema(closes, slow);

        // align: emaSlow starts at index (slow-1), emaFast at (fast-1)
        int offset = slow - fast;
        List<Double> macdLine = new ArrayList<>();
        List<Long> macdTs = new ArrayList<>();
        for (int i = 0; i < emaSlow.size(); i++) {
            macdLine.add(round(emaFast.get(i + offset) - emaSlow.get(i)));
            macdTs.add(candles.get(i + slow - 1).getTs().toEpochMilli());
        }

        List<Double> signalLine = ema(macdLine, signal);
        int sigOffset = signal - 1;
        for (int i = 0; i < signalLine.size(); i++) {
            double m = macdLine.get(i + sigOffset);
            double s = signalLine.get(i);
            result.add(new MacdPoint(macdTs.get(i + sigOffset), m, round(s), round(m - s)));
        }
        return result;
    }

    private List<Double> ema(List<Double> data, int period) {
        List<Double> result = new ArrayList<>();
        double k = 2.0 / (period + 1);
        double prev = data.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        result.add(prev);
        for (int i = period; i < data.size(); i++) {
            prev = data.get(i) * k + prev * (1 - k);
            result.add(round(prev));
        }
        return result;
    }

    // Bollinger %B
    public List<PercentBPoint> calculatePercentB(List<Candle> candles, List<BollingerService.BollingerPoint> bollinger) {
        List<PercentBPoint> result = new ArrayList<>();
        for (BollingerService.BollingerPoint b : bollinger) {
            double bandwidth = b.upper() - b.lower();
            if (bandwidth == 0) continue;
            Candle candle = candles.stream()
                    .filter(c -> c.getTs().toEpochMilli() == b.ts())
                    .findFirst().orElse(null);
            if (candle == null) continue;
            double pctB = (candle.getClose() - b.lower()) / bandwidth;
            result.add(new PercentBPoint(b.ts(), round(pctB)));
        }
        return result;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // EMA overlay (period 9)
    public List<EmaPoint> calculateEma(List<Candle> candles, int period) {
        List<EmaPoint> result = new ArrayList<>();
        if (candles.size() < period) return result;
        List<Double> closes = candles.stream().map(Candle::getClose).toList();
        List<Double> emaValues = ema(closes, period);
        for (int i = 0; i < emaValues.size(); i++) {
            result.add(new EmaPoint(candles.get(i + period - 1).getTs().toEpochMilli(), emaValues.get(i)));
        }
        return result;
    }

    // Rate of Change — % зміна відносно N свічок назад
    public List<RocPoint> calculateRoc(List<Candle> candles, int period) {
        List<RocPoint> result = new ArrayList<>();
        for (int i = period; i < candles.size(); i++) {
            double prev = candles.get(i - period).getClose();
            if (prev == 0) continue;
            double roc = (candles.get(i).getClose() - prev) / prev * 100;
            result.add(new RocPoint(candles.get(i).getTs().toEpochMilli(), round(roc)));
        }
        return result;
    }

    // Z-Score — відхилення від середнього в одиницях σ
    public List<ZScorePoint> calculateZScore(List<Candle> candles, int period) {
        List<ZScorePoint> result = new ArrayList<>();
        for (int i = period - 1; i < candles.size(); i++) {
            List<Candle> slice = candles.subList(i - period + 1, i + 1);
            double mean = slice.stream().mapToDouble(Candle::getClose).average().orElse(0);
            double std = Math.sqrt(slice.stream().mapToDouble(c -> Math.pow(c.getClose() - mean, 2)).average().orElse(0));
            double z = std == 0 ? 0 : (candles.get(i).getClose() - mean) / std;
            result.add(new ZScorePoint(candles.get(i).getTs().toEpochMilli(), round(z)));
        }
        return result;
    }
}
