package com.bachelor.sensorsmonitoringservice.model.entity;

import com.bachelor.sensorsmonitoringservice.model.enums.ActionType;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Action {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id")
    @JsonIgnoreProperties({"rules", "telemetryData", "device"})
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_level", nullable = false)
    private EventLevel eventLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType type;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> config; // email addresses, telegram chatId, mqtt payload
    
    @Builder.Default
    private Boolean enabled = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}