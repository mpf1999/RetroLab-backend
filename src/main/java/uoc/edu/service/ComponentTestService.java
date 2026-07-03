package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.dto.ComponentTestResponseDTO;
import uoc.edu.model.Component;
import uoc.edu.model.ComponentTest;
import uoc.edu.model.RepairCase;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.RepairCaseRepository;

import java.util.List;

@Service
public class ComponentTestService {

    private final ComponentTestRepository componentTestRepository;
    private final RepairCaseRepository repairCaseRepository;
    private final ComponentRepository componentRepository;

    public ComponentTestService(ComponentTestRepository componentTestRepository, ConsoleModelRepository consoleModelRepository, RepairCaseRepository repairCaseRepository, ComponentRepository componentRepository) {
        this.componentTestRepository = componentTestRepository;
        this.repairCaseRepository = repairCaseRepository;
        this.componentRepository = componentRepository;
    }

    private ComponentTestResponseDTO mapToResponseDTO(ComponentTest componentTest) {
        return new ComponentTestResponseDTO(
                componentTest.getComponentTestId(),
                componentTest.getRepairCase().getRepairCaseId(),
                componentTest.getComponent().getComponentId(),
                componentTest.getComponent().getName(),
                componentTest.getMeasuredVoltage(),
                componentTest.getMeasuredCurrent(),
                componentTest.getMeasuredResistance(),
                componentTest.getTemperature(),
                componentTest.getResult(),
                componentTest.getTestDate(),
                componentTest.getNotes()
        );
    }

    public List<ComponentTestResponseDTO> getAllComponentTests() {
        return componentTestRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ComponentTestResponseDTO getComponentTestById(Long id) {
        ComponentTest componentTest = findComponentTestEntityById(id);
        return mapToResponseDTO(componentTest);
    }

    public List<ComponentTestResponseDTO> getComponentTestsByRepairCaseId(Long repairCaseId) {
        return componentTestRepository.findByRepairCaseId(repairCaseId).stream().map(this::mapToResponseDTO).toList();
    }

    public ComponentTestResponseDTO addComponentTest(ComponentTestRequestDTO componentTestRequestDTO) {
        ComponentTest componentTest = new ComponentTest();
        RepairCase repairCase = findRepairCaseEntityById(componentTestRequestDTO.repairCaseId());
        Component component = findComponentEntityById(componentTestRequestDTO.componentId());

        componentTest.setRepairCase(repairCase);
        componentTest.setComponent(component);
        componentTest.setMeasuredVoltage(componentTestRequestDTO.measuredVoltage());
        componentTest.setMeasuredCurrent(componentTestRequestDTO.measuredCurrent());
        componentTest.setMeasuredResistance(componentTestRequestDTO.measuredResistance());
        componentTest.setTemperature(componentTestRequestDTO.temperature());
        componentTest.setNotes(componentTestRequestDTO.notes());
        componentTest.setResult(componentTestRequestDTO.result());

        ComponentTest savedComponentTest = componentTestRepository.save(componentTest);
        return mapToResponseDTO(savedComponentTest);
    }

    public ComponentTestResponseDTO updateComponentTest(Long id, ComponentTestRequestDTO componentTestRequestDTO) {

        ComponentTest componentTest = findComponentTestEntityById(id);

        RepairCase repairCase = findRepairCaseEntityById(componentTestRequestDTO.repairCaseId());
        Component component = findComponentEntityById(componentTestRequestDTO.componentId());

        componentTest.setRepairCase(repairCase);
        componentTest.setComponent(component);
        componentTest.setMeasuredVoltage(componentTestRequestDTO.measuredVoltage());
        componentTest.setMeasuredCurrent(componentTestRequestDTO.measuredCurrent());
        componentTest.setMeasuredResistance(componentTestRequestDTO.measuredResistance());
        componentTest.setTemperature(componentTestRequestDTO.temperature());
        componentTest.setResult(componentTestRequestDTO.result());
        componentTest.setNotes(componentTestRequestDTO.notes());

        ComponentTest savedComponentTest = componentTestRepository.save(componentTest);

        return mapToResponseDTO(savedComponentTest);
    }

    public void deleteComponentTest(Long id) {
        componentTestRepository.deleteById(id);
    }

    public List<ComponentTestResponseDTO> getComponentTestsByComponentId(Long componentId) {
        return componentTestRepository.findByComponentId(componentId).stream().map(this::mapToResponseDTO).toList();
    }

    private ComponentTest findComponentTestEntityById(Long componentId) {
        return componentTestRepository.findById(componentId).orElseThrow(() -> new RuntimeException("Component test not found"));
    }
    private Component findComponentEntityById(Long componentId) {
        return componentRepository.findById(componentId).orElseThrow(() -> new RuntimeException("Component not found"));
    }
    private RepairCase findRepairCaseEntityById(Long repairCaseId) {
        return repairCaseRepository.findById(repairCaseId).orElseThrow(() -> new RuntimeException("RepairCase not found"));
    }
}
