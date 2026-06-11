package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.model.dto.TelemetryPayload;
import com.bachelor.sensorsmonitoringservice.model.entity.Device;
import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import com.bachelor.sensorsmonitoringservice.model.enums.DeviceStatus;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorStatus;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorType;
import com.bachelor.sensorsmonitoringservice.repository.DeviceRepository;
import com.bachelor.sensorsmonitoringservice.repository.SensorRepository;
import com.bachelor.sensorsmonitoringservice.repository.TelemetryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttIngestService {

    private final Mqtt3AsyncClient mqttClient;
    private final ObjectMapper objectMapper;
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    private final RulesEngineService rulesEngineService;
    private final DefaultRulesService defaultRulesService;

    private static final Map<SensorType, String> DEFAULT_UNITS = Map.of(
            SensorType.TEMPERATURE, "C",
            SensorType.HUMIDITY, "%",
            SensorType.FLAME, "bool",
            SensorType.CO, "bool",
            SensorType.LIGHT, "lux",
            SensorType.CURRENT, "A"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void startMqttSubscription() {
        mqttClient.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to connect to MQTT broker", throwable);
                        return;
                    }
                    log.info("Connected to MQTT broker");
                    subscribeToTelemetry();
                });
    }

    private void subscribeToTelemetry() {
        String topicFilter = "iot/devices/+/telemetry";
        mqttClient.subscribeWith()
                .topicFilter(topicFilter)
                .callback(publish -> {
                    try {
                        String topic = publish.getTopic().toString();
                        String macAddress = topic.split("/")[2];
                        String raw = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                        TelemetryPayload payload = objectMapper.readValue(raw, TelemetryPayload.class);
                        processTelemetry(macAddress, payload);
                    } catch (Exception e) {
                        log.error("Error processing MQTT message", e);
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) log.error("Failed to subscribe", throwable);
                    else log.info("Subscribed to {}", topicFilter);
                });
    }

    @Transactional
    public void processTelemetry(String macAddress, TelemetryPayload payload) {
        if (payload.getSensors() == null || payload.getSensors().isEmpty()) return;

        Device device = deviceRepository.findByMacAddress(macAddress)
                .orElseGet(() -> provisionDevice(macAddress, payload.getDeviceId()));

        device.setStatus(DeviceStatus.ONLINE);
        deviceRepository.save(device);

        Instant ts = payload.getTimestamp() != null ? payload.getTimestamp() : Instant.now();

        // Iterate only over sensors present in this payload
        for (Map.Entry<String, TelemetryPayload.SensorReading> entry : payload.getSensors().entrySet()) {
            String sensorKey = entry.getKey();           // e.g. "temperature_1"
            TelemetryPayload.SensorReading reading = entry.getValue();

            SensorType type = parseSensorType(reading.getType());
            if (type == null) {
                log.warn("Unknown sensor type '{}' for key '{}', skipping", reading.getType(), sensorKey);
                continue;
            }

            Double value = parseValue(reading.getValue());
            if (value == null) {
                log.warn("Null value for sensor '{}', skipping", sensorKey);
                continue;
            }

            saveTelemetry(device, sensorKey, type, value, ts);
        }
    }

    private void saveTelemetry(Device device, String sensorKey, SensorType type, Double value, Instant ts) {
        Sensor sensor = sensorRepository.findByDeviceIdAndSensorKey(device.getId(), sensorKey)
                .orElseGet(() -> provisionSensor(device, sensorKey, type));

        telemetryRepository.save(Telemetry.builder()
                .sensor(sensor)
                .ts(ts)
                .value(value)
                .quality(1)
                .build());

        sensor.setLastValue(value);
        sensor.setLastUpdatedAt(ts);
        sensorRepository.save(sensor);

        rulesEngineService.evaluateRules(sensor.getId(), value, ts);
    }

    private Device provisionDevice(String macAddress, String deviceId) {
        log.info("Auto-provisioning device: mac={}, name={}", macAddress, deviceId);
        return deviceRepository.save(Device.builder()
                .macAddress(macAddress)
                .name(deviceId != null ? deviceId : "Device-" + macAddress.substring(macAddress.length() - 6))
                .status(DeviceStatus.ONLINE)
                .build());
    }

    private Sensor provisionSensor(Device device, String sensorKey, SensorType type) {
        log.info("Auto-provisioning sensor: key={}, type={}, device={}", sensorKey, type, device.getMacAddress());
        String unit = DEFAULT_UNITS.getOrDefault(type, "");
        String name = capitalize(type.name()) + " (" + device.getName() + ")";
        Sensor sensor = sensorRepository.save(Sensor.builder()
                .device(device)
                .sensorKey(sensorKey)
                .type(type)
                .name(name)
                .unit(unit)
                .status(SensorStatus.NORMAL)
                .build());
        defaultRulesService.seedRulesForSensor(sensor);
        return sensor;
    }

    private SensorType parseSensorType(String type) {
        if (type == null) return null;
        try {
            return SensorType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Double parseValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof Boolean) return (Boolean) value ? 1.0 : 0.0;
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
