package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterActionListResponse {
    private List<CharacterActionResponse> basicActions;
    private List<CharacterActionResponse> specialActions;
}
