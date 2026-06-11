# ML Prediction Frontend Integration

## Overview
Integrated ML prediction data from the backend services (MLMonitoringService, MLServiceClient) into the frontend React application.

## Changes Made

### 1. API Service Enhancement (`frontend/src/services/api.ts`)
Added new endpoint to fetch all ML predictions:
```typescript
getAllPredictions: () => api.get<MLPrediction[]>('/ml/predictions')
```

### 2. Dashboard Enhancement (`frontend/src/components/Dashboard.tsx`)
- Added ML predictions state management
- Fetches predictions alongside other dashboard data
- Displays ML prediction metrics:
  - Total ML Predictions count
  - Critical Forecasts count
  - Warning Forecasts count
- Gracefully handles ML service unavailability

### 3. Sensors Page Enhancement (`frontend/src/components/SensorsPage.tsx`)
- Added ML prediction column to sensors table
- Shows forecast value and status for each sensor
- Displays prediction status chip (OK/WARNING/CRITICAL)
- Maps predictions to sensors using sensor_id

## Existing Features (Already Implemented)

### ForecastsPage (`frontend/src/components/ForecastsPage.tsx`)
- Dedicated page for viewing all ML forecasts
- Grid layout showing forecast cards for all sensors
- Accessible via `/forecasts` route

### MLForecastCard (`frontend/src/components/MLForecastCard.tsx`)
- Individual sensor forecast visualization
- Shows:
  - 60-second forecast value
  - Risk score with progress bar
  - Status indicator (OK/WARNING/CRITICAL)
  - GRU and ARIMA model scores
- Auto-refreshes every 30 seconds

## Backend Endpoints Used

### GET `/api/ml/predictions`
Returns all ML predictions for active sensors:
```json
[
  {
    "sensor_id": "temp-1",
    "forecast": 67.2,
    "gru_score": 0.85,
    "arima_score": 0.78,
    "risk_score": 0.42,
    "status": "WARNING"
  }
]
```

### GET `/api/ml/predict/sensor/{sensorId}`
Returns ML prediction for a specific sensor with optional threshold parameter.

## Data Flow

1. **MLMonitoringService** (Backend)
   - Scheduled task runs every 10 seconds
   - Fetches last 120 telemetry values per sensor
   - Calls ML service via MLServiceClient
   - Logs predictions and alerts

2. **MLPredictionController** (Backend)
   - Exposes REST endpoints
   - `/api/ml/predictions` - all predictions
   - `/api/ml/predict/sensor/{id}` - single sensor prediction

3. **Frontend Components**
   - Dashboard: Shows summary metrics
   - SensorsPage: Shows inline predictions
   - ForecastsPage: Dedicated forecast view
   - MLForecastCard: Detailed sensor forecast

## Status Mapping

| Status | Color | Meaning |
|--------|-------|---------|
| OK | Green | Normal operation |
| WARNING | Orange | Approaching threshold |
| CRITICAL | Red | Emergency predicted |

## Usage

### View All Forecasts
Navigate to `/forecasts` to see all sensor predictions in a grid layout.

### View Sensor Predictions
Navigate to `/sensors` to see predictions inline with sensor data.

### Dashboard Overview
The main dashboard (`/`) shows aggregate ML prediction metrics.

## Configuration

ML Service configuration in `application.properties`:
```properties
ml.service.url=http://localhost:5000
ml.service.timeout=5000
ml.service.retry.max-attempts=3
ml.service.retry.backoff=1000
```

## Error Handling

- ML service unavailability is handled gracefully
- Failed predictions don't break the UI
- Empty predictions show "N/A" in the interface
- Errors are logged but don't affect other features
