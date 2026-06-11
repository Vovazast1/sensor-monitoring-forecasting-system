package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.events.SensorStatusChangedEvent;
import com.bachelor.sensorsmonitoringservice.model.entity.Action;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorStatus;
import com.bachelor.sensorsmonitoringservice.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ActionExecutorService {

    @Value("${monitoring.action.cooldown-seconds:300}")
    private long cooldownSeconds;

    private final ActionRepository actionRepository;
    private final Map<String, Instant> lastFiredAt = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private EmailNotificationService emailService;

    public ActionExecutorService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }
    // TODO: Add other action executors (Telegram, MQTT, etc.)
    
    public void executeActionsForEvent(SensorStatusChangedEvent event) {
        EventLevel eventLevel = determineEventLevel(event.getNewStatus());
        log.debug("[ActionExecutor] sensor={} newStatus={} → eventLevel={}",
                event.getSensor().getSensorKey(), event.getNewStatus(), eventLevel);
        if (eventLevel == null) {
            log.debug("[ActionExecutor] No eventLevel for status {}, skipping", event.getNewStatus());
            return;
        }

        List<Action> allEnabled = actionRepository.findByEnabledTrue();
        log.debug("[ActionExecutor] Found {} enabled actions total", allEnabled.size());

        List<Action> actions = allEnabled.stream()
                .filter(a -> {
                    boolean match = a.getEventLevel() == eventLevel;
                    if (!match) log.debug("[ActionExecutor] Action {} skipped — level {} != {}", a.getId(), a.getEventLevel(), eventLevel);
                    return match;
                })
                .filter(a -> {
                    boolean match = a.getSensor() == null || a.getSensor().getId().equals(event.getSensor().getId());
                    if (!match) log.debug("[ActionExecutor] Action {} skipped — sensor {} != {}", a.getId(), a.getSensor().getId(), event.getSensor().getId());
                    return match;
                })
                .toList();

        log.debug("[ActionExecutor] {} actions matched for sensor={} level={}", actions.size(), event.getSensor().getSensorKey(), eventLevel);

        for (Action action : actions) {
            String cooldownKey = event.getSensor().getId() + ":" + eventLevel + ":" + action.getId();
            Instant last = lastFiredAt.get(cooldownKey);
            if (last != null && Instant.now().isBefore(last.plusSeconds(cooldownSeconds))) {
                log.debug("[ActionExecutor] Action {} skipped — cooldown active, last fired at {}", action.getId(), last);
                continue;
            }
            try {
                executeAction(action, event);
                lastFiredAt.put(cooldownKey, Instant.now());
                log.info("[ActionExecutor] Successfully executed action {} ({}) for sensor {}",
                        action.getId(), action.getType(), event.getSensor().getSensorKey());
            } catch (Exception e) {
                log.error("[ActionExecutor] Failed to execute action {} ({}) for sensor {}: {}",
                         action.getId(), action.getType(), event.getSensor().getSensorKey(), e.getMessage(), e);
            }
        }
    }
    
    private EventLevel determineEventLevel(SensorStatus status) {
        if (status.isCritical()) {
            return EventLevel.EMERGENCY;
        } else if (status.isWarning()) {
            return EventLevel.WARNING;
        }
        return null; // No actions for NORMAL status
    }
    
    private void executeAction(Action action, SensorStatusChangedEvent event) {
        log.debug("[ActionExecutor] Executing action id={} type={} emailServicePresent={}",
                action.getId(), action.getType(), emailService != null);
        switch (action.getType()) {
            case EMAIL -> {
                if (emailService != null) emailService.sendNotification(action, event);
                else log.warn("[ActionExecutor] Email service not configured (bean missing) — skipping email action {}", action.getId());
            }
            case TELEGRAM -> log.warn("Telegram notifications not implemented yet");
            case MQTT_COMMAND -> log.warn("MQTT commands not implemented yet");
            default -> log.warn("Unknown action type: {}", action.getType());
        }
    }
}