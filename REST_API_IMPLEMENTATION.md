# REST API Implementation Summary

## ✅ Completed Implementation

### 1. Dependencies Added
- **WebFlux** - Added to `build.gradle` for reactive WebClient support
- Enables non-blocking REST calls to ML service

### 2. Configuration
**File:** `application.properties`
```properties
ml.service.url=http://localhost:8000
ml.service.timeout=5000
ml.service.retry.max-attempts=3
ml.service.retry.backoff=1000
```

### 3. DTOs Created

#### MLPredictRequest.java
- `sensorId` - Sensor identifier
- `values` - List of 120 double values
- `threshold` - Alert threshold value

#### MLPredictResponse.java
- `sensorId` - Sensor identifier
- `forecast` - 60s ahead prediction
- `gruScore` - GRU model risk score
- `arimaScore` - ARIMA model risk score
- `riskScore` - Ensemble risk score (60% GRU + 40% ARIMA)
- `status` - OK | WARNING | CRITICAL

### 4. Services Created

#### MLServiceClient.java
- WebClient-based REST client
- Connects to Python ML service at `http://localhost:8000`
- Features:
  - 5-second timeout
  - 3 retry attempts with 1s backoff
  - Logging for success/failure
  - Reactive (non-blocking) calls

#### MLMonitoringService.java
- Scheduled service (runs every 10 seconds)
- Monitors all sensors automatically
- For each sensor:
  1. Fetches last 120 telemetry values
  2. Gets threshold from rules
  3. Calls ML prediction service
  4. Logs warnings for WARNING/CRITICAL status
- Can be extended to trigger alerts/actions

### 5. Controllers Created

#### MLPredictionController.java
**Endpoints:**

1. `POST /api/ml/predict`
   - Direct prediction with provided values
   - Request body: MLPredictRequest
   - Response: MLPredictResponse

2. `GET /api/ml/predict/sensor/{sensorId}?threshold=30.0`
   - Automatic prediction for sensor
   - Fetches last 120 telemetry values
   - Calls ML service
   - Returns prediction response

### 6. Existing Controllers (Already Implemented)

#### DeviceController.java
- `GET /api/devices` - List all devices
- `GET /api/devices/{id}` - Get device by ID
- `POST /api/devices` - Create device
- `PUT /api/devices/{id}` - Update device

#### SensorController.java
- `GET /api/sensors` - List all sensors
- `GET /api/sensors/{id}` - Get sensor by ID
- `GET /api/sensors/{id}/telemetry` - Get sensor telemetry (paginated)
- `GET /api/sensors/{id}/telemetry/latest` - Get latest telemetry

#### EventController.java
- `GET /api/events` - List events (paginated)
- `GET /api/events/active` - List active events

### 7. Application Configuration
- Added `@EnableScheduling` to main application class
- Enables scheduled ML monitoring

---

## 🔄 Integration Flow

```
┌─────────────┐
│   ESP32     │
│   Devices   │
└──────┬──────┘
       │ MQTT
       ▼
┌─────────────────────┐
│  MqttIngestService  │
│  (Spring Boot)      │
└──────┬──────────────┘
       │ Store
       ▼
┌─────────────────────┐
│   PostgreSQL DB     │
│   (Telemetry)       │
└──────┬──────────────┘
       │ Query (every 10s)
       ▼
┌─────────────────────┐
│ MLMonitoringService │
│  (Scheduled)        │
└──────┬──────────────┘
       │ REST Call
       ▼
┌─────────────────────┐
│  ML Service         │
│  (Python/FastAPI)   │
│  Port 8000          │
└──────┬──────────────┘
       │ Prediction
       ▼
┌─────────────────────┐
│  Alert/Action       │
│  (if WARNING/       │
│   CRITICAL)         │
└─────────────────────┘
```

---

## 🚀 Usage Examples

### Start Services

**1. Start ML Service (Python):**
```bash
cd sensor-ml-service
python3 -m pip install -r requirements.txt
python3 run.py
```

**2. Start Spring Boot Service:**
```bash
./gradlew bootRun
```

### Test Endpoints

**Get all sensors:**
```bash
curl http://localhost:8080/api/sensors
```

**Get sensor telemetry:**
```bash
curl http://localhost:8080/api/sensors/1/telemetry?size=10
```

**ML Prediction (manual):**
```bash
curl -X POST http://localhost:8080/api/ml/predict \
  -H "Content-Type: application/json" \
  -d @prediction_request.json
```

**ML Prediction (automatic):**
```bash
curl http://localhost:8080/api/ml/predict/sensor/1?threshold=30.0
```

---

## 📊 Monitoring

### Logs to Watch

**ML Service Client:**
```
ML Service Client initialized with URL: http://localhost:8000
Sending prediction request for sensor: temp-1
Prediction received for sensor temp-1: status=WARNING, risk=0.65
```

**ML Monitoring Service:**
```
ML Prediction for sensor temp-1: status=WARNING, risk=0.65, forecast=32.5
Alert triggered for sensor temp-1: WARNING - Risk Score: 0.65
```

---

## 🔧 Configuration Options

### Adjust Monitoring Frequency
Change `@Scheduled(fixedDelay = 10000)` in MLMonitoringService.java
- 10000 = 10 seconds
- 60000 = 1 minute

### Adjust ML Service Timeout
Change in `application.properties`:
```properties
ml.service.timeout=10000  # 10 seconds
```

### Adjust Retry Logic
```properties
ml.service.retry.max-attempts=5
ml.service.retry.backoff=2000  # 2 seconds
```

---

## 📝 Next Steps

1. **Extend Alert System**
   - Integrate with ActionExecutorService
   - Send email/telegram notifications
   - Execute MQTT commands

2. **Add Event Storage**
   - Create Event entity for ML predictions
   - Store WARNING/CRITICAL events in DB
   - Link to existing events system

3. **Dashboard Integration**
   - Add ML prediction charts to React frontend
   - Show risk scores in real-time
   - Display forecast trends

4. **Performance Optimization**
   - Cache telemetry queries
   - Batch predictions for multiple sensors
   - Optimize database queries

5. **Testing**
   - Unit tests for ML client
   - Integration tests for controllers
   - Load testing for scheduled service

---

## 📚 Documentation

- **REST_API.md** - Complete API documentation
- **README.md** - Project overview
- **IMPLEMENTATION.md** - ML service details

---

## ✨ Key Features

✅ Reactive REST client (non-blocking)
✅ Automatic retry with backoff
✅ Scheduled monitoring (every 10s)
✅ Configurable timeouts and thresholds
✅ Comprehensive logging
✅ CORS enabled for frontend
✅ Paginated telemetry queries
✅ Error handling and validation
