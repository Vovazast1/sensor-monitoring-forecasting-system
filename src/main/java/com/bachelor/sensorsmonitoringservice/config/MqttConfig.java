package com.bachelor.sensorsmonitoringservice.config;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Bean
    public Mqtt3AsyncClient mqttClient() {
        URI brokerUri = URI.create(brokerUrl);
        
        return MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(brokerUri.getHost())
                .serverPort(brokerUri.getPort())
                .buildAsync();
    }
}