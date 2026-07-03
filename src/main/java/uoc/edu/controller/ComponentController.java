package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.dto.ComponentResponseDTO;
import uoc.edu.service.ComponentService;

import java.util.List;

@RestController
@RequestMapping("api/v1/components")
public class ComponentController {

    private final ComponentService componentService;

    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    @GetMapping
    public List<ComponentResponseDTO> getComponents() {
        return componentService.getAllComponents();
    }

    @GetMapping("{id}")
    public ComponentResponseDTO getComponentById(@PathVariable Long id) {
        return componentService.getComponentById(id);
    }

    @PostMapping
    public ResponseEntity<ComponentResponseDTO> addComponent(@Valid @RequestBody ComponentRequestDTO component) {
        return ResponseEntity.status(HttpStatus.CREATED).body(componentService.addComponent(component));
    }

    @PutMapping("{id}")
    public ResponseEntity<ComponentResponseDTO> updateComponent(@PathVariable Long id,@Valid @RequestBody ComponentRequestDTO componentRequestDTO) {
        return ResponseEntity.ok(componentService.updateComponent(id, componentRequestDTO));
    }

    @DeleteMapping("{id}")
    public void deleteComponent(@PathVariable Long id) {
        componentService.deleteComponent(id);
    }
}
