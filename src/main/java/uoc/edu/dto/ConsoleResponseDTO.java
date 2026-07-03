package uoc.edu.dto;

public record ConsoleResponseDTO(Long consoleId,
                                 Long consoleModelId,
                                 String consoleModelName,
                                 String manufacturerName,
                                 String serialNumber,
                                 String region,
                                 String color,
                                 String condition,
                                 String status,
                                 String notes
                                 ) {
}
