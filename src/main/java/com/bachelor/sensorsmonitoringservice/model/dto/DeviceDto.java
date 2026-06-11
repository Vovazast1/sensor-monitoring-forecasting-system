package com.bachelor.sensorsmonitoringservice.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceDto {
    private Long id;
    private String macAddress;
    private String name;
    private String status;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
}