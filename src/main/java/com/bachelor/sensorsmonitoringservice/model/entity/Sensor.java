package com.bachelor.sensorsmonitoringservice.model.entity;

import com.bachelor.sensorsmonitoringservice.model.enums.SensorType;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sensor_key", nullable = false)
    private String sensorKey; // temp-1, hum-1
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnoreProperties({"sensors", "telemetryData", "rules"})
    private Device device;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType type;
    
    private String unit; // C, %, A, V
    
    @Column(nullable = false)
    private String name; // "Boiler Temperature"
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SensorStatus status = SensorStatus.NORMAL;
    
    // Computed properties for frontend compatibility
    public String getCurrentStatus() {
        return status != null ? status.name() : "NORMAL";
    }
    
    public boolean getIsActive() {
        return status != SensorStatus.OFFLINE;
    }
    
    public String getLastUpdated() {
        return lastUpdatedAt != null ? lastUpdatedAt.toString() : null;
    }
    
    private Double lastValue;
    
    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnoreProperties({"sensor", "device"})
    private List<Telemetry> telemetryData = new ArrayList<>();
    
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnoreProperties({"sensor", "device"})
    private List<Rule> rules = new ArrayList<>();
}