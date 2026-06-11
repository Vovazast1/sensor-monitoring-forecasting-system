package com.bachelor.sensorsmonitoringservice.model.dto;

import lombok.Data;

@Data
public class SensorThresholdsRequest {
    private Double lowCritical;
    private Double lowWarning;
    private Double normal;
    private Double highWarning;
    private Double highCritical;
}
