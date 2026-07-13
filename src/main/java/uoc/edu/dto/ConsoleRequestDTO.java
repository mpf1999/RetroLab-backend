package uoc.edu.dto;

import jakarta.validation.constraints.*;
import uoc.edu.model.Condition;
import uoc.edu.model.Status;

import java.math.BigDecimal;

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

        @NotNull(message = "Estimated price is required")
        @DecimalMin(value = "0.0", message = "Estimated price cannot be negative")
        @Digits(integer = 5, fraction = 2)
        BigDecimal estimatedPrice,

        @NotNull(message = "Condition is required")
        Condition condition,

        @NotNull(message = "Status is required")
        Status status,

        @Size(max = 2000, message = "Maximum length is 2000 characters")
        String notes){
}

