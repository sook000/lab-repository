package org.infinity.sixtalebackend.domain.rule.dto;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.infinity.sixtalebackend.domain.rule.domain.JobAction;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActionOptionResponse {
    private Long id;
    private String content;
}
