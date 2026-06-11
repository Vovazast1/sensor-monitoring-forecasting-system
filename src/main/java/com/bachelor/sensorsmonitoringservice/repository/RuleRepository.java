package com.bachelor.sensorsmonitoringservice.repository;

import com.bachelor.sensorsmonitoringservice.model.entity.Rule;
import com.bachelor.sensorsmonitoringservice.model.enums.EventLevel;
import com.bachelor.sensorsmonitoringservice.model.enums.RuleKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    
    List<Rule> findBySensorId(Long sensorId);
    
    List<Rule> findByEnabledTrue();
    
    List<Rule> findByLevelAndEnabledTrue(EventLevel level);
    
    List<Rule> findByRuleKindAndEnabledTrue(RuleKind ruleKind);
}