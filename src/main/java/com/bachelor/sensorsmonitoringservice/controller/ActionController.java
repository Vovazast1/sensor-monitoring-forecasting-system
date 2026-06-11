package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.entity.Action;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.repository.ActionRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ActionController {

    private final ActionRepository actionRepository;
    private final SensorRepository sensorRepository;

    @GetMapping
    public List<Action> getAll() {
        return actionRepository.findAll();
    }

    @PostMapping
    public Action create(@RequestBody Map<String, Object> body) {
        return actionRepository.save(buildAction(new Action(), body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Action> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return actionRepository.findById(id)
                .map(a -> ResponseEntity.ok(actionRepository.save(buildAction(a, body))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!actionRepository.existsById(id)) return ResponseEntity.notFound().build();
        actionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("unchecked")
    private Action buildAction(Action a, Map<String, Object> body) {
        if (body.containsKey("eventLevel"))
            a.setEventLevel(com.bachelor.sensorsmonitoringservice.model.enums.EventLevel.valueOf((String) body.get("eventLevel")));
        if (body.containsKey("type"))
            a.setType(com.bachelor.sensorsmonitoringservice.model.enums.ActionType.valueOf((String) body.get("type")));
        if (body.containsKey("config"))
            a.setConfig((Map<String, Object>) body.get("config"));
        if (body.containsKey("enabled"))
            a.setEnabled((Boolean) body.get("enabled"));
        Object sensorId = body.get("sensorId");
        if (sensorId != null) {
            Long sid = sensorId instanceof Number ? ((Number) sensorId).longValue() : Long.parseLong(sensorId.toString());
            sensorRepository.findById(sid).ifPresent(a::setSensor);
        } else {
            a.setSensor(null);
        }
        return a;
    }
}
