package com.matchingservice.client;

import com.matchingservice.dto.ml.MLBatchMatchRequest;
import com.matchingservice.dto.ml.MLBatchMatchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ML-MATCHING-SERVICE", url = "http://ML-MATCHING-SERVICE:8001", configuration = FeignClientConfig.class)
public interface MLMatchingClient {

    @PostMapping("/api/ml/batch-match")
    MLBatchMatchResponse batchMatch(@RequestBody MLBatchMatchRequest request);
}
