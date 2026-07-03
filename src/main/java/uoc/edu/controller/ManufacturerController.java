package uoc.edu.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.dto.ManufacturerResponseDTO;
import uoc.edu.model.Manufacturer;
import uoc.edu.service.ManufacturerService;

import java.util.List;

@RestController
@RequestMapping("api/v1/manufacturers")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    public List<ManufacturerResponseDTO> getManufacturers() {
        return manufacturerService.getAllManufacturers();
    }

    @GetMapping("{id}")
    public ManufacturerResponseDTO getManufacturerById(@PathVariable Long id) {
        return manufacturerService.getManufacturerById(id);
    }

    @PostMapping
    public ResponseEntity<ManufacturerResponseDTO> addManufacturer(@Valid @RequestBody ManufacturerRequestDTO manufacturerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manufacturerService.addManufacturer(manufacturerRequestDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<ManufacturerResponseDTO> updateManufacturer(@PathVariable Long id,@Valid @RequestBody ManufacturerRequestDTO manufacturerRequestDTO) {
        return ResponseEntity.ok(manufacturerService.updateManufacturer(id, manufacturerRequestDTO));
    }

    @DeleteMapping("{id}")
    public void deleteManufacturer(@PathVariable Long id) {
        manufacturerService.deleteManufacturer(id);
    }
}
