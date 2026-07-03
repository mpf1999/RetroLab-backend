package uoc.edu.dto;

import uoc.edu.model.TestResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComponentTestResponseDTO(
        Long componentTestId,
        Long repairCaseId,
        Long componentId,
        String componentName,
        BigDecimal measuredVoltage,
        BigDecimal measuredCurrent,
        BigDecimal measuredResistance,
        BigDecimal temperature,
        TestResult result,
        LocalDateTime testDate,
        String notes
) {
}
