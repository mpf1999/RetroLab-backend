package uoc.edu.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiValidationError(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> errors
) {
}
