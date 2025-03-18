package org.infinity.sixtalebackend.domain.rule.dto;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.infinity.sixtalebackend.domain.model.DiceType;
import org.infinity.sixtalebackend.domain.rule.domain.ActionOption;
import org.infinity.sixtalebackend.domain.rule.domain.Job;
import org.infinity.sixtalebackend.domain.rule.domain.Rule;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobActionResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Boolean isCore;
    private String description;
    private Boolean isDice;
    private DiceType diceType;
    private Integer diceCount;
    private Integer level;
    private List<ActionOptionResponse> actionOptions;
}
