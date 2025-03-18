package org.infinity.sixtalebackend.domain.equipment.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class EquipmentListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EquipmentResponse> equipments;
}
