package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Role;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = findUserEntityById(userId);
        return mapToResponseDTO(user);
    }

    public UserResponseDTO addUser(UserRequestDTO userRequestDTO) {
        userRepository.findByEmail(userRequestDTO.email())
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException("A user with this email already exists");
                });
        User user = new User();

        user.setEmail(userRequestDTO.email());
        user.setPasswordHash(userRequestDTO.passwordHash());//cambiar por seguridad mas tarde
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long userId, UserRequestDTO userRequestDTO) {
        User existingUser = findUserEntityById(userId);

        existingUser.setEmail(userRequestDTO.email());
        if (userRequestDTO.passwordHash() != null && !userRequestDTO.passwordHash().isBlank()) {
            existingUser.setPasswordHash(userRequestDTO.passwordHash()); // cambiar por BCrypt más tarde
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToResponseDTO(updatedUser);
    }

    public void deleteUser(Long userId) {
        User user = findUserEntityById(userId);
        userRepository.delete(user);
    }

    public UserResponseDTO changeRole(Long userId, Role role) {
        User user = findUserEntityById(userId);
        user.setRole(role);
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
