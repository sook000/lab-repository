package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.*;
import org.infinity.sixtalebackend.domain.rule.domain.Belief;
import org.infinity.sixtalebackend.domain.rule.domain.Job;
import org.infinity.sixtalebackend.domain.rule.domain.JobBeliefID;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobBeliefResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long beliefID;
    private String beliefName;
    private String description;
}
