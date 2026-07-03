package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.dto.RepairCaseResponseDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Console;
import uoc.edu.model.RepairCase;
import uoc.edu.model.RepairStatus;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairCaseService {

    private final RepairCaseRepository repairCaseRepository;
    private final ConsoleRepository consoleRepository;

    public RepairCaseService(RepairCaseRepository repairCaseRepository, ConsoleRepository consoleRepository) {
        this.repairCaseRepository = repairCaseRepository;
        this.consoleRepository = consoleRepository;
    }

    public List<RepairCaseResponseDTO> getAllRepairCases() {

        return repairCaseRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public RepairCaseResponseDTO getRepairCaseById(Long id) {
        RepairCase existing = findRepairEntityCaseById(id);
        return mapToResponseDTO(existing);
    }

    public List<RepairCaseResponseDTO> getRepairCasesByConsoleId(Long consoleId) {
        return repairCaseRepository.findByConsoleId(consoleId).stream().map(this::mapToResponseDTO).toList();
    }

    public RepairCaseResponseDTO addRepairCase(RepairCaseRequestDTO repairCaseRequestDTO) {
        Console console = findConsoleEntityCaseById(repairCaseRequestDTO.consoleId());
        RepairCase repairCase = new RepairCase();

        repairCase.setTitle(repairCaseRequestDTO.title());
        repairCase.setDescription(repairCaseRequestDTO.description());
        repairCase.setStatus(repairCaseRequestDTO.status());
        repairCase.setStartDate(LocalDateTime.now());
        repairCase.setEndDate(null);
        repairCase.setConsole(console);

        RepairCase savedRepairCase = repairCaseRepository.save(repairCase);
        return mapToResponseDTO(savedRepairCase);
    }



    public RepairCaseResponseDTO updateRepairCase(Long id, RepairCaseRequestDTO repairCaseRequestDTO) {
        RepairCase existingRepairCase = findRepairEntityCaseById(id);

        existingRepairCase.setTitle(repairCaseRequestDTO.title());
        existingRepairCase.setDescription(repairCaseRequestDTO.description());
        existingRepairCase.setStatus(repairCaseRequestDTO.status());
        if (repairCaseRequestDTO.consoleId() != null) {
            existingRepairCase.setConsole(findConsoleEntityCaseById(repairCaseRequestDTO.consoleId()));
        }

        if (repairCaseRequestDTO.status() == RepairStatus.CLOSED) {
            existingRepairCase.setEndDate(LocalDateTime.now());
        }

        RepairCase savedRepairCase = repairCaseRepository.save(existingRepairCase);
        return mapToResponseDTO(savedRepairCase);
    }

    public void deleteRepairCase(Long id) {
        RepairCase repairCase = findRepairEntityCaseById(id);

        repairCaseRepository.delete(repairCase);
    }
    public RepairCase findRepairEntityCaseById(Long id) {
        return repairCaseRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("RepairCase not found"));
    }
    private Console findConsoleEntityCaseById(Long id) {
        return consoleRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Console not found"));
    }
    private RepairCaseResponseDTO mapToResponseDTO(RepairCase repairCase) {
        return new RepairCaseResponseDTO(
                repairCase.getRepairCaseId(),
                repairCase.getConsole().getConsoleId(),
                repairCase.getConsole().getConsoleModel().getConsoleModelName(),
                repairCase.getTitle(),
                repairCase.getDescription(),
                repairCase.getStatus(),
                repairCase.getStartDate(),
                repairCase.getEndDate()
        );
    }
}
