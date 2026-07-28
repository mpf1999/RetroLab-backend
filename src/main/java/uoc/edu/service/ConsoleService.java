package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;

import java.util.List;

@Service
public class ConsoleService {

    private final ConsoleRepository consoleRepository;
    private final ConsoleModelRepository consoleModelRepository;
    private final UserRepository userRepository;
    private final RepairCaseRepository repairCaseRepository;

    public ConsoleService(ConsoleRepository consoleRepository, ConsoleModelRepository consoleModelRepository, UserRepository userRepository, RepairCaseRepository repairCaseRepository) {
        this.consoleRepository = consoleRepository;
        this.consoleModelRepository = consoleModelRepository;
        this.userRepository = userRepository;
        this.repairCaseRepository = repairCaseRepository;
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
                console.getOwner().getId(),
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
        console.setEstimatedValue(consoleRequestDTO.estimatedValue());
        console.setStatus(consoleRequestDTO.status());
        console.setNotes(consoleRequestDTO.notes());

        consoleRepository.save(console);

        return mapToResponseDTO(console);
    }

    public ConsoleResponseDTO updateConsole(Long id, ConsoleRequestDTO consoleRequestDTO) {
        Console existingConsole = findConsoleEntityById(id);
        ConsoleModel consoleModel = findConsoleModelEntityById(consoleRequestDTO.consoleModelId());
        User owner = findUserEntityById(consoleRequestDTO.ownerId());

        consoleRepository. findBySerialNumber(consoleRequestDTO.serialNumber()).filter(console-> !console.getConsoleId().equals(id)).ifPresent(
                console -> {
                    throw new ResourceAlreadyExistsException("Trying to update with invalid serialNumber " + consoleRequestDTO.serialNumber() + " belonging to console with id " + id);
                }
        );

        existingConsole.setConsoleModel(consoleModel);
        existingConsole.setOwner(owner);
        existingConsole.setSerialNumber(consoleRequestDTO.serialNumber());
        existingConsole.setRegion(consoleRequestDTO.region());
        existingConsole.setColor(consoleRequestDTO.color());
        existingConsole.setCondition(consoleRequestDTO.condition());
        existingConsole.setEstimatedValue(consoleRequestDTO.estimatedValue());
        existingConsole.setStatus(consoleRequestDTO.status());
        existingConsole.setNotes(consoleRequestDTO.notes());

        Console updated = consoleRepository.save(existingConsole);

        return mapToResponseDTO(updated);
    }

    public void deleteConsole(Long id) {
        Console console = findConsoleEntityById(id);

        if(repairCaseRepository.existsByConsoleConsoleIdAndStatusNot(id, RepairStatus.CLOSED)){
            throw new ResourceInUseException("Console with id " + id + " cannot be deleted because it has active repair cases");
        }
        consoleRepository.delete(console);
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
