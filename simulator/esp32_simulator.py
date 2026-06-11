#!/usr/bin/env python3

import json
import time
import random
import paho.mqtt.client as mqtt
from datetime import datetime

class ESP32Simulator:
    def __init__(self, device_id, broker_host="localhost", broker_port=1883):
        self.device_id = device_id
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.client = mqtt.Client()
        self.sequence = 0
        
        # Sensor configuration
        self.sensors = {
            "temp-1": {"type": "temperature", "unit": "C", "min": 15, "max": 35},
            "hum-1": {"type": "humidity", "unit": "%", "min": 30, "max": 80},
            "cur-1": {"type": "current", "unit": "A", "min": 0.1, "max": 5.0}
        }
        
    def connect(self):
        try:
            self.client.connect(self.broker_host, self.broker_port, 60)
            print(f"✅ Connected to MQTT broker at {self.broker_host}:{self.broker_port}")
            return True
        except Exception as e:
            print(f"❌ Failed to connect to MQTT broker: {e}")
            return False
    
    def generate_telemetry(self):
        values = {}
        for sensor_id, config in self.sensors.items():
            # Generate realistic sensor values with some variation
            base_value = random.uniform(config["min"], config["max"])
            # Add some noise
            value = base_value + random.uniform(-2, 2)
            # Ensure within bounds
            value = max(config["min"], min(config["max"], value))
            
            # Use sensor type as key (temperature, humidity, current)
            values[config["type"]] = round(value, 2)
        
        self.sequence += 1
        
        payload = {
            "deviceId": self.device_id,
            "ts": datetime.now().isoformat() + "Z",
            "values": values
        }
        
        return json.dumps(payload)
    
    def publish_telemetry(self):
        topic = f"iot/devices/{self.device_id}/telemetry"
        payload = self.generate_telemetry()
        
        result = self.client.publish(topic, payload)
        if result.rc == 0:
            print(f"📡 Published telemetry: {payload}")
        else:
            print(f"❌ Failed to publish telemetry")
    
    def run(self, interval=10):
        if not self.connect():
            return
        
        print(f"🤖 Starting ESP32 simulator for device: {self.device_id}")
        print(f"📊 Publishing telemetry every {interval} seconds")
        print("Press Ctrl+C to stop")
        
        try:
            while True:
                self.publish_telemetry()
                time.sleep(interval)
        except KeyboardInterrupt:
            print("\n🛑 Stopping simulator...")
            self.client.disconnect()

if __name__ == "__main__":
    import sys
    
    device_id = sys.argv[1] if len(sys.argv) > 1 else "esp32-001"
    interval = int(sys.argv[2]) if len(sys.argv) > 2 else 1
    
    simulator = ESP32Simulator(device_id)
    simulator.run(interval)