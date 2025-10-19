package com.matchingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionConfirmationDTO {

    @NotBlank(message = "Please confirm you received the donation")
    private String confirmationMessage;

    private LocalDate receivedDate;

    @Size(max = 500)
    private String notes;

    private Integer rating;

    private String hospitalName;
}
