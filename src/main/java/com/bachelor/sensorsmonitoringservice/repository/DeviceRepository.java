package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Device;
import com.bachelor.sensorsmonitoringservice.model.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    List<Device> findByStatus(DeviceStatus status);
    
    Optional<Device> findByMacAddress(String macAddress);
}