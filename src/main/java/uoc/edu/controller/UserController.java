package uoc.edu.controller;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.ChangeRoleRequestDTO;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.model.Role;
import uoc.edu.service.UserService;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("{userId}")
    public UserResponseDTO getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(userRequestDTO));
    }

    @PutMapping("{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequestDTO));
    }

    @DeleteMapping("{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PatchMapping("{userId}/role")
    public ResponseEntity<UserResponseDTO> changeUserRole(@PathVariable Long userId, @RequestBody ChangeRoleRequestDTO role) {
        return ResponseEntity.ok(userService.changeRole(userId, role.role()));
    }
}
