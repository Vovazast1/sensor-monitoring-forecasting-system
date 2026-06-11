package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Device;
import com.bachelor.sensorsmonitoringservice.model.enums.DeviceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void shouldSaveAndFindDevice() {
        // Given
        Device device = Device.builder()
                .id(1L)
                .name("Test Device")
                .location("Test Location")
                .status(DeviceStatus.ONLINE)
                .build();

        // When
        Device saved = deviceRepository.save(device);
        Device found = deviceRepository.findById(1L).orElse(null);

        // Then
        assertThat(saved).isNotNull();
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Device");
        assertThat(found.getStatus()).isEqualTo(DeviceStatus.ONLINE);
    }
}