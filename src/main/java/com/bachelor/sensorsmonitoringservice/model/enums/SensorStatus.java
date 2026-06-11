package com.bachelor.sensorsmonitoringservice.model.enums;

public enum SensorStatus {
    LOW_CRITICAL(1, "Low Critical"),
    LOW_WARNING(2, "Low Warning"),
    NORMAL(3, "Normal"),
    HIGH_WARNING(4, "High Warning"),
    HIGH_CRITICAL(5, "High Critical"),
    ERROR(7, "Sensor Error"),
    OFFLINE(14, "Offline"),
    UNREACHABLE(15, "Unreachable"),
    UNKNOWN(0, "Unknown Status");
    
    private final Integer value;
    private final String description;
    
    SensorStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static SensorStatus fromValue(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (SensorStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
    
    public boolean isWarning() {
        return this == HIGH_WARNING || this == LOW_WARNING;
    }
    
    public boolean isCritical() {
        return this == HIGH_CRITICAL || this == LOW_CRITICAL;
    }
    
    public boolean isNormal() {
        return this == NORMAL;
    }
}