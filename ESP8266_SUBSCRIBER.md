# ESP8266 MQTT Subscriber

## Implementation

The Spring Boot service subscribes to `sensors/data` topic and processes ESP8266 telemetry.

### Components

1. **ESP8266Payload.java** - DTO matching ESP8266 JSON format
2. **ESP8266SubscriberService.java** - MQTT subscriber that:
   - Connects to broker at `192.168.1.105:1883`
   - Subscribes to `sensors/data` topic
   - Parses JSON payload
   - Stores telemetry in database
   - Updates sensor status

### Configuration

Update `application.properties`:
```properties
mqtt.broker.url=tcp://192.168.1.105:1883
```

### Data Flow

```
ESP8266 → MQTT Broker → Spring Boot → PostgreSQL
         (sensors/data)  (Subscriber)   (Telemetry)
```

### Testing

1. Start PostgreSQL
2. Run Spring Boot app
3. Flash ESP8266 with provided code
4. Check logs:
```
Received: {"sensorId":"sensor-1","value":4.692}
Saved telemetry: sensor=sensor-1, value=4.692
```

### Database

Telemetry stored in `telemetry` table:
- sensor_id: 1 (maps to "sensor-1")
- ts: current timestamp
- value: from ESP8266
- quality: 1 (good)

Sensor updated in `sensors` table:
- last_value: latest reading
- last_updated_at: current timestamp
- status: NORMAL
