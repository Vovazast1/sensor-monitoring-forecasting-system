package com.bachelor.sensorsmonitoringservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLCollectRequest {

    @JsonProperty("sensor_id")
    private String sensorId;

    private List<Double> values;

    @JsonProperty("actual_value")
    private Double actualValue;
}
