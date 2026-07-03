package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.dto.ComponentTestResponseDTO;
import uoc.edu.service.ComponentTestService;

import java.util.List;

@RestController
@RequestMapping("api/v1/component-tests")
public class ComponentTestController {

    private final ComponentTestService componentTestService;

    public ComponentTestController(ComponentTestService componentTestService) {
        this.componentTestService = componentTestService;
    }

    @GetMapping
    public List<ComponentTestResponseDTO> getComponentTests() {
        return componentTestService.getAllComponentTests();
    }

    @GetMapping("{id}")
    public ComponentTestResponseDTO getComponentTestById(@PathVariable Long id) {
        return componentTestService.getComponentTestById(id);
    }

    @GetMapping("repair-case/{repairCaseId}")
    public List<ComponentTestResponseDTO> getComponentTestsByRepairCaseId(@PathVariable Long repairCaseId) {
        return componentTestService.getComponentTestsByRepairCaseId(repairCaseId);
    }

    @GetMapping("component/{componentId}")
    public List<ComponentTestResponseDTO> getComponentTestsByComponentId(@PathVariable Long componentId) {
        return componentTestService.getComponentTestsByComponentId(componentId);
    }

    @PostMapping
    public ResponseEntity<ComponentTestResponseDTO> addComponentTest(@Valid @RequestBody ComponentTestRequestDTO componentTestRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(componentTestService.addComponentTest(componentTestRequestDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<ComponentTestResponseDTO> updateComponentTest(@PathVariable Long id,@Valid @RequestBody ComponentTestRequestDTO componentTestRequestDTO) {
        return ResponseEntity.ok(componentTestService.updateComponentTest(id, componentTestRequestDTO));
    }

    @DeleteMapping("{id}")
    public void deleteComponentTest(@PathVariable Long id) {
        componentTestService.deleteComponentTest(id);
    }
}