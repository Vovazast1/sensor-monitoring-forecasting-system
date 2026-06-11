-- Minimal schema for IoT sensors monitoring system

CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    mac_address VARCHAR(17) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'UNKNOWN',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sensors (
    id BIGSERIAL PRIMARY KEY,
    sensor_key VARCHAR(255) NOT NULL,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    unit VARCHAR(10),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'NORMAL',
    last_value DOUBLE PRECISION,
    last_updated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, sensor_key)
);

CREATE TABLE telemetry (
    id BIGSERIAL PRIMARY KEY,
    sensor_id BIGINT NOT NULL REFERENCES sensors(id) ON DELETE CASCADE,
    ts TIMESTAMP WITH TIME ZONE NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    quality INTEGER DEFAULT 1
);

CREATE INDEX idx_sensor_ts ON telemetry(sensor_id, ts DESC);
CREATE INDEX idx_ts ON telemetry(ts DESC);

CREATE TABLE rules (
    id BIGSERIAL PRIMARY KEY,
    sensor_id BIGINT REFERENCES sensors(id) ON DELETE CASCADE,
    level VARCHAR(20) NOT NULL,
    rule_kind VARCHAR(30) NOT NULL,
    threshold_value DOUBLE PRECISION,
    window_size INTEGER DEFAULT 10,
    min_increases INTEGER DEFAULT 7,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE actions (
    id BIGSERIAL PRIMARY KEY,
    event_level VARCHAR(20) NOT NULL,
    type VARCHAR(30) NOT NULL,
    config JSONB,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sensors_device ON sensors(device_id);
CREATE INDEX idx_sensors_status ON sensors(status);
CREATE INDEX idx_rules_sensor ON rules(sensor_id);
CREATE INDEX idx_actions_level ON actions(event_level);
