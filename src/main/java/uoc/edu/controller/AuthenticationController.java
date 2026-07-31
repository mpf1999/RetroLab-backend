package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.AuthenticationResponseDTO;
import uoc.edu.dto.LoginRequestDTO;
import uoc.edu.security.JwtService;
import uoc.edu.security.PrivUserDetailsService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final PrivUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            PrivUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.email());

        String token = jwtService.generateToken(userDetails);

        AuthenticationResponseDTO response =
                new AuthenticationResponseDTO(
                        token,
                        jwtService.getExpirationTime()
                );

        return ResponseEntity.ok(response);
    }
}