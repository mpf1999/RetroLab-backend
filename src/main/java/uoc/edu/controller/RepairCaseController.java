package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.dto.RepairCaseResponseDTO;
import uoc.edu.service.RepairCaseService;
import uoc.edu.service.UserService;

import java.util.List;

@RestController
@RequestMapping("api/v1/repair-cases")
public class RepairCaseController {

    private final RepairCaseService repairCaseService;

    public RepairCaseController(RepairCaseService repairCaseService, UserService userService) {
        this.repairCaseService = repairCaseService;
    }

    @GetMapping
    public List<RepairCaseResponseDTO> getRepairCases() {
        return repairCaseService.getAllRepairCases();
    }

    @GetMapping("{id}")
    public RepairCaseResponseDTO getRepairCaseById(@PathVariable Long id) {
        return repairCaseService.getRepairCaseById(id);
    }

    @GetMapping("consoles/{consoleId}")
    public List<RepairCaseResponseDTO> getRepairCasesByConsoleId(@PathVariable Long consoleId) {
        return repairCaseService.getRepairCasesByConsoleId(consoleId);
    }

    @PostMapping
    public ResponseEntity<RepairCaseResponseDTO> addRepairCase(@Valid @RequestBody RepairCaseRequestDTO repairCaseRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repairCaseService.addRepairCase(repairCaseRequestDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<RepairCaseResponseDTO> updateRepairCase(@PathVariable Long id,@Valid @RequestBody RepairCaseRequestDTO repairCaseRequestDTO) {
        return ResponseEntity.ok(repairCaseService.updateRepairCase(id, repairCaseRequestDTO));
    }

    @DeleteMapping("{id}")
    public void deleteRepairCase(@PathVariable Long id) {
        repairCaseService.deleteRepairCase(id);
    }
}
