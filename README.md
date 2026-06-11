# Sensors Monitoring Service

IoT система моніторингу датчиків з автоматизованим оповіщенням та прогнозуванням аварійних станів.

## Архітектура

### Технології
- **Backend**: Java 21 + Spring Boot 3
- **Database**: PostgreSQL + Flyway міграції
- **MQTT**: HiveMQ MQTT Client
- **Build**: Gradle

### Структура БД

#### Основні сутності:
1. **devices** - ESP32 пристрої
2. **sensors** - датчики на пристроях (температура, вологість, струм)
3. **telemetry** - часові ряди даних з датчиків
4. **rules** - правила для WARNING/EMERGENCY (пороги + тренди)
5. **events** - події (попередження/аварії)
6. **actions** - дії на події (email, telegram, mqtt команди)
7. **action_executions** - лог виконання дій

#### Типи правил:
- **THRESHOLD** - перевищення порогу
- **TREND** - тренд зростання (останні N значень)
- **SUSTAINED_THRESHOLD** - тривале перевищення порогу

#### Рівні подій:
- **WARNING** - передаварійний стан
- **EMERGENCY** - аварійна ситуація

### Моделі та Репозиторії

#### Entity класи:
- `Device` - пристрої ESP32
- `Sensor` - датчики з типами (TEMPERATURE, HUMIDITY, CURRENT, etc.)
- `Telemetry` - дані з індексами для швидкого пошуку
- `Rule` - правила моніторингу
- `Event` - події з станами (OPEN/CLOSED)
- `Action` - дії з JSON конфігурацією
- `ActionExecution` - аудит виконання дій

#### Repository інтерфейси:
- Всі репозиторії наслідують `JpaRepository`
- Додаткові методи для пошуку за критеріями
- Оптимізовані запити для телеметрії

### Міграції

#### V1__init_schema.sql
- Створення всіх таблиць
- Індекси для оптимізації (особливо для telemetry)
- Foreign key constraints

#### V2__test_data.sql
- Тестові пристрої (esp32-001, esp32-002)
- Тестові датчики (temp-1, hum-1, cur-1, etc.)
- Базові правила для температури
- Дії для email та MQTT команд

## Запуск

### Вимоги
- Java 21
- PostgreSQL
- Gradle

### База даних
```sql
CREATE DATABASE sensors_monitoring;
```

### Конфігурація
Налаштування в `application.properties`:
- PostgreSQL підключення
- JPA/Hibernate
- Flyway міграції

### Компіляція
```bash
./gradlew compileJava
```

### Тести
```bash
./gradlew test
```

## Веб-інтерфейс

### Швидкий запуск
```bash
./start.sh
```

### Доступ до додатку
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **MQTT Broker**: localhost:1883

### Функціональність
- 📊 **Dashboard** - огляд системи та графіки
- 🔧 **Devices** - управління ESP32 пристроями
- 📡 **Sensors** - моніторинг датчиків в реальному часі
- 🚨 **Events** - перегляд попереджень та аварій

### Технології
- **Frontend**: React + TypeScript + Material-UI
- **Backend**: Spring Boot REST API
- **Infrastructure**: Docker Compose

## Наступні кроки

1. ✅ **MQTT Ingest Service** - підписка на телеметрію
2. ✅ **Rules Engine** - обробка правил WARNING/EMERGENCY
3. **Trend Analyzer** - аналіз трендів (останні N значень)
4. **Notification Service** - відправка email/telegram
5. ✅ **REST API** - для веб-інтерфейсу
6. ✅ **React Frontend** - дашборд та адмінка

## MQTT Протокол

### Topic Structure
```
iot/{deviceId}/telemetry
iot/{deviceId}/status
iot/{deviceId}/commands
```

### Payload Format
```json
{
  "deviceId": "esp32-001",
  "ts": 1769115600,
  "seq": 10234,
  "readings": [
    { "sensorId": "temp-1", "type": "temperature", "value": 67.2, "unit": "C" },
    { "sensorId": "hum-1", "type": "humidity", "value": 38.1, "unit": "%" }
  ]
}
```