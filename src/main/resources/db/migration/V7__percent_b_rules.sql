-- %B WARNING: sensor goes above 0.9 (approaching upper Bollinger band)
INSERT INTO rules (sensor_id, level, rule_kind, threshold_value, threshold_direction, window_size, enabled)
SELECT id, 'WARNING', 'PERCENT_B', 0.9, 'HIGH', 20, true
FROM sensors WHERE sensor_key = 'temp-1';

-- %B EMERGENCY: sensor breaks above 1.0 (outside upper Bollinger band)
INSERT INTO rules (sensor_id, level, rule_kind, threshold_value, threshold_direction, window_size, enabled)
SELECT id, 'EMERGENCY', 'PERCENT_B', 1.0, 'HIGH', 20, true
FROM sensors WHERE sensor_key = 'temp-1';
