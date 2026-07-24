package uoc.edu.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uoc.edu.dto.ChangeRoleRequestDTO;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = findUserEntityById(userId);
        return mapToResponseDTO(user);
    }

    public UserResponseDTO addUser(UserRequestDTO userRequestDTO) {

        String normalEmail = userRequestDTO.email().trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(normalEmail)
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException("A user with this email already exists");
                });
        User user = new User();

        user.setEmail(userRequestDTO.email());
        user.setName(userRequestDTO.name());
        user.setPasswordHash(passwordEncoder.encode(userRequestDTO.password()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(userRequestDTO.role());

        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long userId, UserRequestDTO userRequestDTO) {
        User existingUser = findUserEntityById(userId);
        String normalEmail = userRequestDTO.email().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(normalEmail)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException(
                            "A user with this email already exists");
                });
        existingUser.setName(userRequestDTO.name());
        existingUser.setEmail(userRequestDTO.email());
        if (userRequestDTO.password() != null && !userRequestDTO.password().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userRequestDTO.password()));
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToResponseDTO(updatedUser);
    }

    public void deleteUser(Long userId) {
        User user = findUserEntityById(userId);
        userRepository.delete(user);
    }

    public UserResponseDTO changeRole(Long userId, ChangeRoleRequestDTO roleRequestDTO) {
        User user = findUserEntityById(userId);
        if(user.getRole() == roleRequestDTO.role()) {
            throw new InvalidRequestException("User already has this role");
        }

        user.setRole(roleRequestDTO.role());
        userRepository.save(user);

        return mapToResponseDTO(user);
    }
    private User findUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponseDTO mapToResponseDTO(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
