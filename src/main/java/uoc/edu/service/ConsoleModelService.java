package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ConsoleModelRequestDTO;
import uoc.edu.dto.ConsoleModelResponseDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ManufacturerRepository;

import java.time.Year;
import java.util.List;

@Service
public class ConsoleModelService {

    private final ConsoleModelRepository consoleModelRepository;
    private final ManufacturerRepository manufacturerRepository;

    public ConsoleModelService(ConsoleModelRepository consoleModelRepository, ManufacturerRepository manufacturerRepository) {
        this.consoleModelRepository = consoleModelRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    public List<ConsoleModelResponseDTO> getAllConsoleModels() {
        return consoleModelRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ConsoleModelResponseDTO getConsoleModelById(Long id) {
        ConsoleModel consoleModel = findConsoleModelEntityById(id);

        return mapToResponseDTO(consoleModel);
    }

    public ConsoleModelResponseDTO addConsoleModel(ConsoleModelRequestDTO consoleModelRequestDTO) {
        Manufacturer m = findManufacturerEntityById(consoleModelRequestDTO.manufacturerId());
        ConsoleModel cm = new ConsoleModel();

        cm.setReleaseYear(consoleModelRequestDTO.releaseYear());
        cm.setConsoleModelName(consoleModelRequestDTO.consoleModelName());
        cm.setManufacturer(m);

        ConsoleModel savedConsoleModel = consoleModelRepository.save(cm);
        return mapToResponseDTO(savedConsoleModel);
    }

    public ConsoleModelResponseDTO updateConsoleModel(Long id, ConsoleModelRequestDTO consoleModelRequestDTO) {
        ConsoleModel existingConsoleModel = findConsoleModelEntityById(id);
        Manufacturer manufacturer = findManufacturerEntityById(id);

        existingConsoleModel.setConsoleModelName(consoleModelRequestDTO.consoleModelName());
        existingConsoleModel.setReleaseYear(consoleModelRequestDTO.releaseYear());
        if(existingConsoleModel.getReleaseYear() > Year.now().getValue()) {
            throw new InvalidRequestException("Release year out of range");
        }

        existingConsoleModel.setManufacturer(manufacturer);
        ConsoleModel savedConsoleModel = consoleModelRepository.save(existingConsoleModel);
        return mapToResponseDTO(savedConsoleModel);
    }

    public void deleteConsoleModel(Long id) {
        ConsoleModel consoleModel = findConsoleModelEntityById(id);
        consoleModelRepository.delete(consoleModel);
    }

    private ConsoleModel findConsoleModelEntityById(Long id) {
        return consoleModelRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Console model not found"));
    }

    private Manufacturer findManufacturerEntityById(Long id) {
        return manufacturerRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Associated manufacturer not found"));
    }
    private ConsoleModelResponseDTO mapToResponseDTO(ConsoleModel consoleModel) {
        return new ConsoleModelResponseDTO(
                consoleModel.getConsoleModelId(),
                consoleModel.getConsoleModelName(),
                consoleModel.getReleaseYear(),
                consoleModel.getManufacturer().getManufacturerId(),
                consoleModel.getManufacturer().getManufacturerName()
        );
    }
}
