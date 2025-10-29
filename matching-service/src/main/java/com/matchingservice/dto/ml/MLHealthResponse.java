// MLHealthResponse.java
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
public class MLHealthResponse {
    private String status;
    private List<String> modelsLoaded;
    private String version;
}
