package uoc.edu.dto;

public record AuthenticationResponseDTO(
        String token,
        long expiresIn
) {
}
