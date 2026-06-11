package com.bachelor.sensorsmonitoringservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class TelemetryPayload {

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("sensors")
    private Map<String, SensorReading> sensors;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @Data
    public static class SensorReading {
        @JsonProperty("type")
        private String type;

        @JsonProperty("value")
        private Object value; // Double or Boolean
    }
}
