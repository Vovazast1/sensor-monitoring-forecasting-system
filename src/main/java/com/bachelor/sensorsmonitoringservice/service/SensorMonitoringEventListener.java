package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.events.SensorStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorMonitoringEventListener {

    private final ActionExecutorService actionExecutorService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleSensorStatusChanged(SensorStatusChangedEvent event) {
        log.info("Sensor {} status changed from {} to {} ({})",
                event.getSensor().getSensorKey(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getMessage());
        actionExecutorService.executeActionsForEvent(event);
    }
}