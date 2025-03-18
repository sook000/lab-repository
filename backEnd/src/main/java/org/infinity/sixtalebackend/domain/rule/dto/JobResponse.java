package org.infinity.sixtalebackend.domain.rule.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.infinity.sixtalebackend.domain.model.DiceType;
import org.infinity.sixtalebackend.domain.rule.domain.JobBelief;
import org.infinity.sixtalebackend.domain.rule.domain.JobRace;
import org.infinity.sixtalebackend.domain.rule.domain.Rule;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private Long id;
    private String name;
    private String description;
    private Integer hp;
    private Integer weight;
    private DiceType diceType;
    private String imageURL;
    private Long ruleId;
    private Set<JobBeliefResponse> jobBeliefs;
    private Set<JobRaceResponse> jobRaces;
}
