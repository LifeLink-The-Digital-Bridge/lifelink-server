package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrantDetailsDTO {

    private String aadhaarHash;
    private Double latitude;
    private Double longitude;
}
