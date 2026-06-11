package com.bachelor.sensorsmonitoringservice.model.entity;

import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.RuleKind;
import com.bachelor.sensorsmonitoringservice.model.enums.ThresholdDirection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventLevel level;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_kind", nullable = false)
    private RuleKind ruleKind;
    
    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "threshold_direction")
    @Builder.Default
    private ThresholdDirection thresholdDirection = ThresholdDirection.HIGH;
    
    @Column(name = "window_size")
    @Builder.Default
    private Integer windowSize = 10;
    
    @Column(name = "min_increases")
    @Builder.Default
    private Integer minIncreases = 7;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}