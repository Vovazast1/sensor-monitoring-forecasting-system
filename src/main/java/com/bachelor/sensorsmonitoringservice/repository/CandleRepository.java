package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandleRepository extends JpaRepository<Candle, Long> {

    @Query("SELECT c FROM Candle c WHERE c.sensor.id = :sensorId ORDER BY c.ts DESC LIMIT :limit")
    List<Candle> findLastN(@Param("sensorId") Long sensorId, @Param("limit") int limit);

    @Query("SELECT c FROM Candle c WHERE c.sensor.id = :sensorId AND c.ts >= :from ORDER BY c.ts ASC")
    List<Candle> findBySensorIdSince(@Param("sensorId") Long sensorId, @Param("from") Instant from);

    Optional<Candle> findTopBySensorIdOrderByTsDesc(Long sensorId);

    boolean existsBySensorIdAndTs(Long sensorId, Instant ts);
}
