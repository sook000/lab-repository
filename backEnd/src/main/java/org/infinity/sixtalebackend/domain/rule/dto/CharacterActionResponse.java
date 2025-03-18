package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.infinity.sixtalebackend.domain.model.DiceType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterActionResponse {
    private Long id;
    private Long actionID;
    private String name;
    private String description;
    private Boolean isDice;
    private DiceType diceType;
    private Integer diceCount;
}
