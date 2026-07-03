package uoc.edu.dto;

import jakarta.validation.constraints.*;

public record ComponentRequestDTO(
        @NotBlank(message = "Component name is required")
        @Size(min = 2, max = 80, message = "Component name must be between 2 and 80 characters")
        String name,

        @NotBlank(message = "Component description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}
