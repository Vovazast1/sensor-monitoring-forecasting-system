package com.bachelor.sensorsmonitoringservice.controller;

import com.bachelor.sensorsmonitoringservice.model.dto.DeviceDto;
import com.bachelor.sensorsmonitoringservice.model.entity.Device;
import com.bachelor.sensorsmonitoringservice.model.enums.DeviceStatus;
import com.bachelor.sensorsmonitoringservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DeviceController {
    
    private final DeviceRepository deviceRepository;
    
    @GetMapping
    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private DeviceDto convertToDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .macAddress(device.getMacAddress())
                .name(device.getName())
                .status(device.getStatus() != null ? device.getStatus().name() : "UNKNOWN")
                .isActive(device.getStatus() != DeviceStatus.OFFLINE)
                .createdAt(device.getCreatedAt() != null ? device.getCreatedAt().toString() : null)
                .updatedAt(device.getUpdatedAt() != null ? device.getUpdatedAt().toString() : null)
                .build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDevice(@PathVariable Long id) {
        return deviceRepository.findById(id)
                .map(d -> ResponseEntity.ok(convertToDto(d)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        return deviceRepository.save(device);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return deviceRepository.findById(id)
                .map(existing -> {
                    device.setId(id);
                    return ResponseEntity.ok(deviceRepository.save(device));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}