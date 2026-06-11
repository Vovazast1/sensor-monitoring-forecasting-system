# Запуск веб-додатку

## Швидкий старт з Docker

1. **Запуск всієї системи:**
```bash
cd infra
docker-compose up -d
```

2. **Доступ до додатку:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- MQTT Broker: localhost:1883

## Локальна розробка

### Backend (Spring Boot)
```bash
# Запуск PostgreSQL
docker run -d --name postgres -p 5432:5432 -e POSTGRES_DB=sensors_monitoring -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15

# Запуск MQTT
docker run -d --name mosquitto -p 1883:1883 -p 9001:9001 eclipse-mosquitto:2.0

# Запуск backend
./gradlew bootRun
```

### Frontend (React)
```bash
cd frontend
npm install
npm start
```

## API Endpoints

### Devices
- `GET /api/devices` - список пристроїв
- `GET /api/devices/{id}` - деталі пристрою
- `POST /api/devices` - створити пристрій
- `PUT /api/devices/{id}` - оновити пристрій

### Sensors
- `GET /api/sensors` - список датчиків
- `GET /api/sensors/{id}` - деталі датчика
- `GET /api/sensors/{id}/telemetry` - телеметрія датчика
- `GET /api/sensors/{id}/telemetry/latest` - останні дані

### Events
- `GET /api/events` - всі події
- `GET /api/events/active` - активні події

## Структура проєкту

```
sensors-monitoring-service/
├── src/main/java/                 # Spring Boot backend
│   └── com/bachelor/sensorsmonitoringservice/
│       ├── controller/            # REST API контролери
│       ├── entity/               # JPA сутності
│       ├── repository/           # Репозиторії
│       ├── service/              # Бізнес логіка
│       └── config/               # Конфігурація
├── frontend/                     # React frontend
│   ├── src/
│   │   ├── components/           # React компоненти
│   │   └── services/             # API сервіси
│   └── public/
├── infra/                        # Docker конфігурація
│   ├── docker-compose.yml
│   └── mosquitto.conf
└── src/main/resources/
    └── db/migration/             # Flyway міграції
```

## Технології

**Backend:**
- Java 21 + Spring Boot 3
- PostgreSQL + Flyway
- HiveMQ MQTT Client
- JPA/Hibernate

**Frontend:**
- React 18 + TypeScript
- Material-UI (MUI)
- Recharts для графіків
- Axios для API запитів

**Infrastructure:**
- Docker + Docker Compose
- Eclipse Mosquitto MQTT
- Nginx для frontend