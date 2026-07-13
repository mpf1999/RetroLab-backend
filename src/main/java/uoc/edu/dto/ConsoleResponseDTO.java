package uoc.edu.dto;

import uoc.edu.model.Condition;
import uoc.edu.model.Status;

import java.math.BigDecimal;

public record ConsoleResponseDTO(Long consoleId,
                                 Long consoleModelId,
                                 String consoleModelName,
                                 String manufacturerName,
                                 String serialNumber,
                                 String region,
                                 String color,
                                 Condition condition,
                                 BigDecimal estimatedPrice,
                                 Status status,
                                 String notes
                                 ) {
}
