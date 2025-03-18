package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.*;
import org.infinity.sixtalebackend.domain.equipment.domain.Equipment;
import org.infinity.sixtalebackend.domain.equipment.dto.EquipmentResponse;
import org.infinity.sixtalebackend.domain.rule.domain.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOptionListResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<JobRaceResponse> jobRaceList;
    private List<JobBeliefResponse> jobBeliefList;
    private List<EquipmentResponse> jobEquipmentList;
    private List<JobActionResponse> jobActionList;
    private List<List<ActionOption>> actionOptionList;
}
