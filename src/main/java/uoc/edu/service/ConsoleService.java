package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.UserRepository;

import java.util.List;

@Service
public class ConsoleService {

    private final ConsoleRepository consoleRepository;
    private final ConsoleModelRepository consoleModelRepository;
    private final UserRepository userRepository;

    public ConsoleService(ConsoleRepository consoleRepository, ConsoleModelRepository consoleModelRepository, UserRepository userRepository) {
        this.consoleRepository = consoleRepository;
        this.consoleModelRepository = consoleModelRepository;
        this.userRepository = userRepository;
    }

    public List<ConsoleResponseDTO> getAllConsoles() {
        return consoleRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ConsoleResponseDTO getConsoleById(Long id) {
        Console console = findConsoleEntityById(id);
        return mapToResponseDTO(console);
    }

    private ConsoleResponseDTO mapToResponseDTO(Console console) {

        return new ConsoleResponseDTO(
                console.getConsoleId(),
                console.getConsoleModel().getConsoleModelId(),
                console.getConsoleModel().getConsoleModelName(),
                console.getConsoleModel().getManufacturer().getManufacturerName(),
                console.getSerialNumber(),
                console.getRegion(),
                console.getColor(),
                console.getCondition(),
                console.getEstimatedValue(),
                console.getStatus(),
                console.getNotes()
        );
    }

    public ConsoleResponseDTO addConsole(ConsoleRequestDTO consoleRequestDTO) {
        ConsoleModel consoleModel = findConsoleModelEntityById(consoleRequestDTO.consoleModelId());
        User owner = findUserEntityById(consoleRequestDTO.ownerId());

        if(consoleRepository.findBySerialNumber(consoleRequestDTO.serialNumber()).isPresent()){
            throw new ResourceAlreadyExistsException("Console with serial number " + consoleRequestDTO.serialNumber() + " already exists");
        }

        Console console = new Console();
        console.setConsoleModel(consoleModel);
        console.setOwner(owner);
        console.setSerialNumber(consoleRequestDTO.serialNumber());
        console.setRegion(consoleRequestDTO.region());
        console.setColor(consoleRequestDTO.color());
        console.setCondition(consoleRequestDTO.condition());
        console.setEstimatedValue(consoleRequestDTO.estimatedPrice());
        console.setStatus(consoleRequestDTO.status());
        console.setNotes(consoleRequestDTO.notes());

        consoleRepository.save(console);

        return mapToResponseDTO(console);
    }

    public ConsoleResponseDTO updateConsole(Long id, ConsoleRequestDTO consoleRequestDTO) {
        Console existingConsole = findConsoleEntityById(id);
        ConsoleModel consoleModel = consoleModelRepository.findById(consoleRequestDTO.consoleModelId()).orElseThrow(()-> new IllegalArgumentException("Console model not found"));

        consoleRepository.findBySerialNumber(consoleRequestDTO.serialNumber()).filter(console-> !console.getConsoleId().equals(id)).ifPresent(
                console -> {
                    throw new ResourceAlreadyExistsException(consoleRequestDTO.serialNumber());
                }
        );
        existingConsole.setConsoleModel(consoleModel);
        existingConsole.setSerialNumber(consoleRequestDTO.serialNumber());
        existingConsole.setRegion(consoleRequestDTO.region());
        existingConsole.setColor(consoleRequestDTO.color());
        existingConsole.setCondition(consoleRequestDTO.condition());
        existingConsole.setEstimatedValue(consoleRequestDTO.estimatedPrice());
        existingConsole.setStatus(consoleRequestDTO.status());
        existingConsole.setNotes(consoleRequestDTO.notes());

        Console updated = consoleRepository.save(existingConsole);

        return mapToResponseDTO(updated);
    }

    public void deleteConsole(Long id) {
        consoleRepository.deleteById(id);
    }

    private Console findConsoleEntityById(Long id) {
        return consoleRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Console not found"));
    }
    private ConsoleModel findConsoleModelEntityById(Long id) {
        return consoleModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Console model not found"));
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
