package uoc.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConsoleRequestDTO (
        @NotNull(message = "Owner ID cannot be null")
        Long ownerId,

        @NotNull(message = "Console model ID is required")
        Long consoleModelId,

        @NotBlank(message = "Serial number is required")
        @Size(max = 45, message = "Maximum length is 45 characters")
        String serialNumber,

        @NotNull(message = "Region is required")
        String region,

        @NotBlank(message = "Color is required")
        @Size(max = 30)
        String color,

        @NotNull(message = "Condition is required")
        String condition,

        @NotNull(message = "Status is required")
        String status,

        @Size(max = 2000, message = "Maximum length is 2000 characters")
        String notes){
}

