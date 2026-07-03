package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.ConsoleModelRequestDTO;
import uoc.edu.dto.ConsoleModelResponseDTO;
import uoc.edu.model.ConsoleModel;
import uoc.edu.service.ConsoleModelService;

import java.util.List;

@RestController
@RequestMapping("api/v1/console-models")
public class ConsoleModelController {

    private final ConsoleModelService consoleModelService;

    public ConsoleModelController(ConsoleModelService consoleModelService) {
        this.consoleModelService = consoleModelService;
    }

    @GetMapping
    public List<ConsoleModelResponseDTO> getConsoleModels() {
        return consoleModelService.getAllConsoleModels();
    }

    @GetMapping("{id}")
    public ConsoleModelResponseDTO getConsoleModelById(@PathVariable Long id) {
        return consoleModelService.getConsoleModelById(id);
    }

    @PostMapping
    public ResponseEntity<ConsoleModelResponseDTO> addConsoleModel(@Valid @RequestBody ConsoleModelRequestDTO consoleModelRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consoleModelService.addConsoleModel(consoleModelRequestDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<ConsoleModelResponseDTO> updateConsoleModel(@PathVariable Long id,@Valid @RequestBody ConsoleModelRequestDTO consoleModelRequestDTO) {
        return ResponseEntity.ok(consoleModelService.updateConsoleModel(id, consoleModelRequestDTO));
    }

    @DeleteMapping("{id}")
    public void deleteConsoleModel(@PathVariable Long id) {
        consoleModelService.deleteConsoleModel(id);
    }
}
