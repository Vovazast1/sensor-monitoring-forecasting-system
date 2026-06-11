CREATE TABLE candles (
    id          BIGSERIAL PRIMARY KEY,
    sensor_id   BIGINT NOT NULL REFERENCES sensors(id) ON DELETE CASCADE,
    ts          TIMESTAMP WITH TIME ZONE NOT NULL,
    open        DOUBLE PRECISION NOT NULL,
    high        DOUBLE PRECISION NOT NULL,
    low         DOUBLE PRECISION NOT NULL,
    close       DOUBLE PRECISION NOT NULL
);

CREATE UNIQUE INDEX idx_candles_sensor_ts ON candles(sensor_id, ts DESC);
