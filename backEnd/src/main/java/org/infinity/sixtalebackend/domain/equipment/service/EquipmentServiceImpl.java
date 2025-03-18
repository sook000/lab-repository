package org.infinity.sixtalebackend.domain.equipment.service;

import lombok.AllArgsConstructor;
import org.infinity.sixtalebackend.domain.equipment.domain.Equipment;
import org.infinity.sixtalebackend.domain.equipment.dto.EquipmentListResponse;
import org.infinity.sixtalebackend.domain.equipment.dto.EquipmentResponse;
import org.infinity.sixtalebackend.domain.equipment.repository.EquipmentRepository;
import org.infinity.sixtalebackend.domain.rule.domain.Rule;
import org.infinity.sixtalebackend.domain.rule.repository.RuleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final RuleRepository ruleRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    @Cacheable(value = "equipmentListCache", key = "#ruleID", unless = "#result == null")
    public EquipmentListResponse readEquipmentList(Long ruleID) {
        Rule rule = ruleRepository.findById(ruleID).get();
        List<Equipment> equipmentList = equipmentRepository.findByRule(rule);

        List<EquipmentResponse> equipments = equipmentList.stream()
                .map(m -> EquipmentResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .description(m.getDescription())
                        .equipmentType(m.getEquipmentType())
                        .weight(m.getWeight())
                        .count(m.getCount())
                        .ruleId(m.getRule().getId())
                        .jobId(m.getJob().getId())
                        .imageUrl(m.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return EquipmentListResponse.builder().equipments(equipments).build();
    }
}
