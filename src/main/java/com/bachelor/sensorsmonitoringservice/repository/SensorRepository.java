package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Sensor;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    
    List<Sensor> findByDeviceId(Long deviceId);
    
    List<Sensor> findByType(SensorType type);
    
    Optional<Sensor> findBySensorKey(String sensorKey);
    
    @Query("SELECT s FROM Sensor s WHERE s.device.id = :deviceId AND s.sensorKey = :sensorKey")
    Optional<Sensor> findByDeviceIdAndSensorKey(@Param("deviceId") Long deviceId, 
                                                @Param("sensorKey") String sensorKey);
}