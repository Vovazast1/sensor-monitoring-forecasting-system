# Phase 1 Implementation - Service Skeleton

## Completed Work

### 1. Project Structure
```
sensor-ml-service/
├── app/
│   ├── __init__.py
│   ├── main.py          # FastAPI application
│   ├── schemas.py       # Pydantic request/response models
│   └── predictor.py     # Mock prediction logic
├── requirements.txt     # Python dependencies
├── run.py              # Service launcher
├── test_service.py     # Integration tests
└── README.md           # Documentation
```

### 2. API Endpoints

#### `GET /`
Service information and status

#### `GET /health`
Health check endpoint

#### `POST /predict`
Main prediction endpoint

**Request:**
```json
{
  "sensor_id": "sensor_001",
  "values": [20.5, 20.6, ...],  // 120 float values
  "threshold": 30.0
}
```

**Response:**
```json
{
  "sensor_id": "sensor_001",
  "forecast": 25.3,
  "gru_score": 0.2,
  "arima_score": 0.15,
  "risk_score": 0.18,
  "status": "OK"  // OK | WARNING | CRITICAL
}
```

### 3. Mock Prediction Logic

- Linear extrapolation for 60s forecast
- Risk scoring based on threshold proximity
- Ensemble aggregation (60% GRU + 40% ARIMA weights)
- Status classification:
  - `risk_score < 0.4` → OK
  - `0.4 ≤ risk_score < 0.7` → WARNING
  - `risk_score ≥ 0.7` → CRITICAL

### 4. Dependencies

```
fastapi==0.109.0
uvicorn[standard]==0.27.0
pydantic==2.5.3
numpy==1.26.3
```

---

## Spring Boot Integration Requirements

### 1. Add Dependencies (pom.xml)

```xml
<!-- Spring WebClient for REST calls -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### 2. Configuration (application.yml)

```yaml
ml-service:
  url: http://localhost:8000
  timeout: 5000  # 5 seconds
  retry:
    max-attempts: 3
    backoff: 1000  # 1 second
```

### 3. Request DTO

```java
public class MLPredictRequest {
    private String sensorId;
    private List<Double> values;  // 120 values
    private Double threshold;
    
    // getters, setters, constructors
}
```

### 4. Response DTO

```java
public class MLPredictResponse {
    private String sensorId;
    private Double forecast;
    private Double gruScore;
    private Double arimaScore;
    private Double riskScore;
    private String status;  // OK, WARNING, CRITICAL
    
    // getters, setters
}
```

### 5. ML Service Client

```java
@Service
public class MLServiceClient {
    
    private final WebClient webClient;
    
    public MLServiceClient(@Value("${ml-service.url}") String mlServiceUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(mlServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    public Mono<MLPredictResponse> predict(MLPredictRequest request) {
        return webClient.post()
            .uri("/predict")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MLPredictResponse.class)
            .timeout(Duration.ofSeconds(5))
            .retry(3);
    }
}
```

### 6. Integration Flow

```
1. Spring Boot receives sensor data from MQTT
2. Store in TimescaleDB
3. Every 10 seconds:
   - Query last 120 values
   - Call ML service /predict
   - Process response
   - Trigger alert if status = WARNING/CRITICAL
```

### 7. Example Usage

```java
@Service
public class SensorMonitoringService {
    
    @Autowired
    private MLServiceClient mlClient;
    
    @Scheduled(fixedDelay = 10000)  // Every 10 seconds
    public void checkSensors() {
        List<String> sensorIds = getSensorIds();
        
        for (String sensorId : sensorIds) {
            List<Double> values = getLast120Values(sensorId);
            Double threshold = getThreshold(sensorId);
            
            MLPredictRequest request = new MLPredictRequest(
                sensorId, values, threshold
            );
            
            mlClient.predict(request)
                .subscribe(response -> {
                    if ("WARNING".equals(response.getStatus()) || 
                        "CRITICAL".equals(response.getStatus())) {
                        triggerAlert(response);
                    }
                });
        }
    }
}
```

---

## Testing

### Start ML Service
```bash
python3 -m pip install -r requirements.txt
python3 run.py
```

### Test from Command Line
```bash
curl -X POST "http://localhost:8000/predict" \
  -H "Content-Type: application/json" \
  -d '{
    "sensor_id": "sensor_001",
    "values": [20.0, 20.1, 20.2, ...],
    "threshold": 30.0
  }'
```

### Test with Python Script
```bash
python3 test_service.py
```

### Access Swagger UI
```
http://localhost:8000/docs
```

---

## Next Steps

- **Phase 2**: Real data preprocessing & validation
- **Phase 3**: ARIMA model integration
- **Phase 4**: GRU neural network
- **Phase 5**: Risk aggregation refinement
- **Phase 6**: Model training with real sensor data
- **Phase 7**: Performance optimization (<100ms target)
