// MLBatchMatchResponse.java
package com.matchingservice.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLBatchMatchResponse {
    private Boolean success;
    private Integer matchesFound;
    private List<MLMatchResult> matches;
    private String error;
    private String modelVersion;
    private Long processingTimeMs;
}
