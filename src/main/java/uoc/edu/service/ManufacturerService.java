package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.dto.ManufacturerResponseDTO;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ManufacturerRepository;

import java.util.List;

@Service
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    public ManufacturerService(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    public List<ManufacturerResponseDTO> getAllManufacturers() {
        return manufacturerRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ManufacturerResponseDTO getManufacturerById(Long id) {
        Manufacturer m = findManufacturerEntityById(id);
        return mapToResponseDTO(m);
    }

    public ManufacturerResponseDTO addManufacturer(ManufacturerRequestDTO manufacturer) {
        Manufacturer m = new Manufacturer();

        m.setManufacturerName(manufacturer.manufacturerName());
        m.setCountryCode(manufacturer.countryCode());

        Manufacturer savedManufacturer = manufacturerRepository.save(m);
        return mapToResponseDTO(savedManufacturer);
    }

    public ManufacturerResponseDTO updateManufacturer(Long id, ManufacturerRequestDTO manufacturerRequestDTO) {
        Manufacturer existingManufacturer = findManufacturerEntityById(id);

        existingManufacturer.setManufacturerName(manufacturerRequestDTO.manufacturerName());
        existingManufacturer.setCountryCode(manufacturerRequestDTO.countryCode());
        Manufacturer updatedManufacturer = manufacturerRepository.save(existingManufacturer);
        return mapToResponseDTO(updatedManufacturer);
    }

    public void deleteManufacturer(Long id) {
        Manufacturer manufacturer = findManufacturerEntityById(id);
        manufacturerRepository.delete(manufacturer);
    }

    private ManufacturerResponseDTO mapToResponseDTO(Manufacturer manufacturer) {
        return new ManufacturerResponseDTO(
                manufacturer.getManufacturerId(), manufacturer.getManufacturerName(), manufacturer.getCountryCode()
        );
    }
    public Manufacturer findManufacturerEntityById(Long id) {
        return manufacturerRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Manufacturer not found"));
    }
}
