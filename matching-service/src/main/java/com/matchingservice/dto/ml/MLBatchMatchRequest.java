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
public class MLBatchMatchRequest {
    private List<MLRequestData> requests;
    private List<MLDonationData> donations;

    @Builder.Default
    private Integer topN = 10;

    @Builder.Default
    private Double threshold = 0.5;
}
