package uoc.edu.dto;

import jakarta.validation.constraints.*;
import uoc.edu.model.TestResult;

import java.math.BigDecimal;

public record ComponentTestRequestDTO(
        @NotNull(message = "Repair case ID is required")
        Long repairCaseId,

        @NotNull(message = "Component ID is required")
        Long componentId,

        @NotNull(message = "Measured voltage is required")
        @DecimalMin(value = "0.0", message = "Voltage cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Voltage must have up to 5 integer digits and 2 decimal places")
        BigDecimal measuredVoltage,

        @NotNull(message = "Measured current is required")
        @DecimalMin(value = "0.0", message = "Current cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Current must have up to 5 integer digits and 2 decimal places")
        BigDecimal measuredCurrent,
        @NotNull(message = "Measured resistance is required")
        @DecimalMin(value = "0.0", message = "Resistance cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Resistance must have up to 5 integer digits and 2 decimal places")
        BigDecimal measuredResistance,
        @NotNull(message = "Temperature is required")
        @Digits(integer = 3, fraction = 2, message = "Temperature must have up to 3 integer digits and 2 decimal places")
        BigDecimal temperature,
        @NotNull(message = "Test result is required")
        TestResult result,

        @Size(max = 2000, message = "Maximum length is 2000 characters")
        String notes
) {
}
