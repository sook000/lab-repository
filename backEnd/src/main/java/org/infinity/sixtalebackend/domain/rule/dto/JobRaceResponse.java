package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.*;
import org.infinity.sixtalebackend.domain.rule.domain.Job;
import org.infinity.sixtalebackend.domain.rule.domain.JobRaceID;
import org.infinity.sixtalebackend.domain.rule.domain.Race;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRaceResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long raceID;
    private String raceName;
    private String description;
}
