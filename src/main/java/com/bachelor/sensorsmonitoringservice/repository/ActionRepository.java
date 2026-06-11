package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Action;
import com.bachelor.sensorsmonitoringservice.model.enums.ActionType;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    
    List<Action> findByEventLevelAndEnabledTrue(EventLevel eventLevel);

    List<Action> findBySensorIdAndEventLevelAndEnabledTrue(Long sensorId, EventLevel eventLevel);
    
    List<Action> findByTypeAndEnabledTrue(ActionType type);
    
    List<Action> findByEnabledTrue();
}