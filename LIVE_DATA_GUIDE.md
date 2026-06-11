# Live Data Monitoring Guide

## Current System Status ✅

- **Backend API**: Running on http://localhost:8080
- **Frontend**: Running on http://localhost:3000
- **MQTT Broker**: Running on localhost:1883
- **Database**: PostgreSQL with test data

## Where to See Live Data Updates

### 1. Dashboard (http://localhost:3000/)
**Shows:**
- Active devices count
- Active sensors count
- Warning/Emergency events
- **ML Predictions metrics** (new!)
  - Total predictions
  - Critical forecasts
  - Warning forecasts
- Real-time telemetry charts

### 2. Sensors Page (http://localhost:3000/sensors)
**Shows:**
- All sensors with current values
- Status (NORMAL/WARNING/EMERGENCY)
- **ML Forecast column** (new!) - predicted values and risk status
- Last updated timestamp
- Auto-refreshes every 30 seconds

### 3. Forecasts Page (http://localhost:3000/forecasts)
**Shows:**
- ML prediction cards for all sensors
- 60-second forecast values
- Risk scores with progress bars
- GRU and ARIMA model scores
- Status indicators (OK/WARNING/CRITICAL)
- Auto-refreshes every 30 seconds

### 4. Monitoring Page (http://localhost:3000/monitoring)
**Shows:**
- Real-time sensor readings
- Historical data charts

## How to Send Live Data

### Option 1: Use the ESP32 Simulator (Recommended)

```bash
# Start simulator for a device
cd simulator
python3 esp32_simulator.py esp32-001 5

# This will send telemetry every 5 seconds
```

### Option 2: Use MQTT Directly

```bash
# Publish telemetry via mosquitto_pub
mosquitto_pub -h localhost -t "iot/devices/esp32-001/telemetry" -m '{
  "deviceId": "esp32-001",
  "ts": "2026-03-10T20:00:00Z",
  "values": {
    "temperature": 25.5,
    "humidity": 45.2,
    "current": 2.3
  }
}'
```

### Option 3: Use the Test Script

```bash
# Send test telemetry
./test-api.sh
```

## Test Data in Database

The system has pre-populated test data:
- **5 devices**: Boiler Room, Kitchen, Warehouse, Office, Storage
- **13 sensors**: Temperature, Humidity, Current, Light sensors
- **150+ telemetry records** per sensor (for ML predictions)

## How Data Flows

```
ESP32/Simulator → MQTT Broker → MqttIngestService → Database
                                       ↓
                                 RulesEngine
                                       ↓
                              MLMonitoringService
                                       ↓
                                  ML Service
                                       ↓
                                  REST API
                                       ↓
                                  Frontend
```

## Verify Live Updates

1. **Open Frontend**: http://localhost:3000/sensors
2. **Start Simulator**: `cd simulator && python3 esp32_simulator.py esp32-001 5`
3. **Watch Updates**: Sensor values update every 30 seconds
4. **Check ML Predictions**: Navigate to /forecasts to see predictions

## API Endpoints for Testing

```bash
# Get all devices
curl http://localhost:8080/api/devices

# Get all sensors
curl http://localhost:8080/api/sensors

# Get sensor telemetry
curl http://localhost:8080/api/sensors/1/telemetry

# Get ML predictions
curl http://localhost:8080/api/ml/predictions

# Get ML prediction for specific sensor
curl http://localhost:8080/api/ml/predict/sensor/1
```

## Troubleshooting

### Data Not Updating?
1. Check backend logs: `./gradlew bootRun`
2. Check MQTT broker: `docker logs sensors-mosquitto`
3. Verify simulator is running: `ps aux | grep esp32_simulator`

### ML Predictions Not Showing?
1. Ensure ML service is running on port 5000
2. Check backend logs for ML service errors
3. Verify sensors have 120+ telemetry records

### Frontend Not Refreshing?
1. Hard refresh browser (Cmd+Shift+R)
2. Check browser console for errors
3. Verify API is accessible: `curl http://localhost:8080/api/sensors`
