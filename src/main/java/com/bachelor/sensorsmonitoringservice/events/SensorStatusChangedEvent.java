package com.bachelor.sensorsmonitoringservice.events;

import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorStatus;
import lombok.Getter;

@Getter
public class SensorStatusChangedEvent extends SensorMonitoringEvent {
    
    private final Sensor sensor;
    private final SensorStatus previousStatus;
    private final SensorStatus newStatus;
    private final EventLevel level;
    private final String message;
    private final String ruleType;
    private final String deviceName; // resolved while session is open
    
    public SensorStatusChangedEvent(Object source, Sensor sensor, SensorStatus previousStatus, 
                                   SensorStatus newStatus, EventLevel level, String message, String ruleType,
                                   String deviceName) {
        super(source);
        this.sensor = sensor;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.level = level;
        this.message = message;
        this.ruleType = ruleType;
        this.deviceName = deviceName;
    }
}