package org.infinity.sixtalebackend.domain.rule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<JobResponse> jobList;
}
