package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.model.Console;
import uoc.edu.service.ConsoleService;

import java.util.List;

@RestController
@RequestMapping("api/v1/consoles")
public class ConsoleController {
    private final ConsoleService consoleService;

    public ConsoleController(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }

    @GetMapping
    public List<ConsoleResponseDTO> getConsoles() {
        return consoleService.getAllConsoles();
    }

    @GetMapping("{id}")
    public ConsoleResponseDTO getConsoleById(@PathVariable Long id) {
        return consoleService.getConsoleById(id);
    }

    @PostMapping
    public ResponseEntity<ConsoleResponseDTO> addConsole(@Valid @RequestBody ConsoleRequestDTO consoleRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consoleService.addConsole(consoleRequestDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<ConsoleResponseDTO> updateConsole(@PathVariable Long id,@Valid @RequestBody ConsoleRequestDTO consoleRequestDTO) {
        return ResponseEntity.ok(consoleService.updateConsole(id, consoleRequestDTO));
    }

    @DeleteMapping("{id}")
    public void deleteConsole(@PathVariable Long id) {
        consoleService.deleteConsole(id);
    }
}

