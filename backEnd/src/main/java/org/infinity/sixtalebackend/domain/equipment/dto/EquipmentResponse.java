package org.infinity.sixtalebackend.domain.equipment.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.infinity.sixtalebackend.domain.equipment.domain.EquipmentType;
import org.infinity.sixtalebackend.domain.rule.domain.Job;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String description;
    private EquipmentType equipmentType;
    private Integer weight;
    private Integer count;
    private Long ruleId;
    private Long jobId;
    private String imageUrl;
}
