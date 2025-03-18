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
public class RuleInfoResponse {
    // 룰
    private Long id;
    private String title;
    private String description;
    // 직업
    private JobListResponse jobs;
    // 능력치
    private StatListResponse stats;
}
