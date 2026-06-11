package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    
    @Query("SELECT t FROM Telemetry t WHERE t.sensor.id = :sensorId ORDER BY t.ts DESC LIMIT :limit")
    List<Telemetry> findLatestBySensorId(@Param("sensorId") Long sensorId, @Param("limit") int limit);
    
    @Query("SELECT t FROM Telemetry t WHERE t.sensor.id = :sensorId AND t.ts BETWEEN :from AND :to ORDER BY t.ts")
    List<Telemetry> findBySensorIdAndTsBetween(@Param("sensorId") Long sensorId, 
                                               @Param("from") Instant from, 
                                               @Param("to") Instant to);

    Page<Telemetry> findBySensorId(Long sensorId, Pageable pageable);
    
    Optional<Telemetry> findTopBySensorIdOrderByTsDesc(Long sensorId);
}