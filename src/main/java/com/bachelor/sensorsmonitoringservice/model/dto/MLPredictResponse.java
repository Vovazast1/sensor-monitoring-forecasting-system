package com.bachelor.sensorsmonitoringservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictResponse {

    @JsonProperty("sensor_id")
    private String sensorId;

    @JsonProperty("gru_forecast")
    private Double gruForecast;

    @JsonProperty("arima_forecast")
    private Double arimaForecast;
}
