package com.bachelor.sensorsmonitoringservice.events;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

public abstract class SensorMonitoringEvent extends ApplicationEvent {
    
    private final Instant eventTimestamp;
    
    public SensorMonitoringEvent(Object source) {
        super(source);
        this.eventTimestamp = Instant.now();
    }
    
    public Instant getEventTimestamp() {
        return eventTimestamp;
    }
}