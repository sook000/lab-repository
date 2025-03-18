package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatResponse {
    private Long id;
    private String name;
    private Long ruleId;
}
