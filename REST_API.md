# REST API Documentation

## Base URL
```
http://localhost:8080/api
```

## Endpoints

### Devices API

#### Get All Devices
```http
GET /api/devices
```

**Response:**
```json
[
  {
    "id": 1,
    "macAddress": "AA:BB:CC:DD:EE:FF",
    "name": "ESP32-001",
    "status": "ONLINE",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
]
```

#### Get Device by ID
```http
GET /api/devices/{id}
```

#### Create Device
```http
POST /api/devices
Content-Type: application/json

{
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "name": "ESP32-001",
  "status": "ONLINE"
}
```

#### Update Device
```http
PUT /api/devices/{id}
Content-Type: application/json

{
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "name": "ESP32-001 Updated",
  "status": "OFFLINE"
}
```

---

### Sensors API

#### Get All Sensors
```http
GET /api/sensors
```

**Response:**
```json
[
  {
    "id": 1,
    "sensorKey": "temp-1",
    "name": "Boiler Temperature",
    "type": "TEMPERATURE",
    "unit": "C",
    "currentStatus": "NORMAL",
    "isActive": true,
    "lastValue": 65.5,
    "lastUpdated": "2024-01-15T10:30:00Z"
  }
]
```

#### Get Sensor by ID
```http
GET /api/sensors/{id}
```

#### Get Sensor Telemetry
```http
GET /api/sensors/{id}/telemetry?page=0&size=100&from=2024-01-01T00:00:00Z&to=2024-01-15T23:59:59Z
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "ts": "2024-01-15T10:30:00Z",
      "value": 65.5,
      "quality": 1
    }
  ],
  "totalElements": 1000,
  "totalPages": 10,
  "size": 100,
  "number": 0
}
```

#### Get Latest Telemetry
```http
GET /api/sensors/{id}/telemetry/latest
```

**Response:**
```json
{
  "id": "uuid",
  "ts": "2024-01-15T10:30:00Z",
  "value": 65.5,
  "quality": 1
}
```

---

### Events API

#### Get All Events
```http
GET /api/events?page=0&size=20&status=OPEN
```

#### Get Active Events
```http
GET /api/events/active?page=0&size=20
```

---

### ML Prediction API

#### Direct Prediction
```http
POST /api/ml/predict
Content-Type: application/json

{
  "sensor_id": "temp-1",
  "values": [20.0, 20.1, 20.2, ..., 21.5],
  "threshold": 30.0
}
```

**Response:**
```json
{
  "sensor_id": "temp-1",
  "forecast": 25.3,
  "gru_score": 0.2,
  "arima_score": 0.15,
  "risk_score": 0.18,
  "status": "OK"
}
```

**Status Values:**
- `OK` - risk_score < 0.4
- `WARNING` - 0.4 ≤ risk_score < 0.7
- `CRITICAL` - risk_score ≥ 0.7

#### Predict for Sensor
```http
GET /api/ml/predict/sensor/{sensorId}?threshold=30.0
```

Automatically fetches last 120 telemetry values and calls ML service.

**Response:** Same as Direct Prediction

---

## ML Service Integration

### Configuration
```properties
ml.service.url=http://localhost:8000
ml.service.timeout=5000
ml.service.retry.max-attempts=3
ml.service.retry.backoff=1000
```

### Scheduled Monitoring
The system automatically checks all sensors every 10 seconds:
- Fetches last 120 telemetry values
- Calls ML prediction service
- Logs warnings for WARNING/CRITICAL status
- Can be extended to trigger alerts/actions

### Integration Flow
```
1. MQTT → Telemetry data stored in DB
2. Every 10s → MLMonitoringService runs
3. For each sensor:
   - Query last 120 values
   - Call ML service /predict
   - Process response
   - Trigger alert if WARNING/CRITICAL
```

---

## Error Responses

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "path": "/api/sensors/999"
}
```

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient telemetry data"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "ML service unavailable"
}
```

---

## Testing

### cURL Examples

**Get all sensors:**
```bash
curl http://localhost:8080/api/sensors
```

**Get sensor telemetry:**
```bash
curl "http://localhost:8080/api/sensors/1/telemetry?page=0&size=10"
```

**ML Prediction:**
```bash
curl -X POST http://localhost:8080/api/ml/predict \
  -H "Content-Type: application/json" \
  -d '{
    "sensor_id": "temp-1",
    "values": [20.0, 20.1, 20.2, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9, 21.0, 21.1, 21.2, 21.3, 21.4, 21.5, 21.6, 21.7, 21.8, 21.9, 22.0, 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7, 22.8, 22.9, 23.0, 23.1, 23.2, 23.3, 23.4, 23.5, 23.6, 23.7, 23.8, 23.9, 24.0, 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7, 24.8, 24.9, 25.0, 25.1, 25.2, 25.3, 25.4, 25.5, 25.6, 25.7, 25.8, 25.9, 26.0, 26.1, 26.2, 26.3, 26.4, 26.5, 26.6, 26.7, 26.8, 26.9, 27.0, 27.1, 27.2, 27.3, 27.4, 27.5, 27.6, 27.7, 27.8, 27.9, 28.0, 28.1, 28.2, 28.3, 28.4, 28.5, 28.6, 28.7, 28.8, 28.9, 29.0, 29.1, 29.2, 29.3, 29.4, 29.5, 29.6, 29.7, 29.8, 29.9, 30.0, 30.1, 30.2, 30.3, 30.4, 30.5, 30.6, 30.7, 30.8, 30.9, 31.0, 31.1, 31.2, 31.3, 31.4, 31.5, 31.6, 31.7, 31.8, 31.9],
    "threshold": 30.0
  }'
```

**Predict for sensor:**
```bash
curl "http://localhost:8080/api/ml/predict/sensor/1?threshold=30.0"
```
