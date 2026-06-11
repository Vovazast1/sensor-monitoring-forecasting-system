package com.bachelor.sensorsmonitoringservice.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorDto {
    private Long id;
    private String sensorKey;
    private String name;
    private String type;
    private String unit;
    private Long deviceId;
    private String currentStatus;
    private boolean isActive;
    private Double lastValue;
    private String lastUpdated;
}