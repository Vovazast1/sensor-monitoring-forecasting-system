package com.bachelor.sensorsmonitoringservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "telemetry", indexes = {
    @Index(name = "idx_sensor_ts", columnList = "sensor_id, ts DESC"),
    @Index(name = "idx_ts", columnList = "ts DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Telemetry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;
    
    @Column(nullable = false)
    private Instant ts;
    
    @Column(nullable = false)
    private Double value;
    
    @Builder.Default
    private Integer quality = 1; // 0/1 для позначення якості даних
}