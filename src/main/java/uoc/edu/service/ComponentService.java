package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.dto.ComponentResponseDTO;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Component;
import uoc.edu.repository.ComponentRepository;

import java.util.List;

@Service
public class ComponentService {

    private final ComponentRepository componentRepository;

    public ComponentService(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    public List<ComponentResponseDTO> getAllComponents() {
        return componentRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ComponentResponseDTO getComponentById(Long id) {
        Component component = findComponentEntityById(id);
        return mapToResponseDTO(component);
    }

    public ComponentResponseDTO addComponent(ComponentRequestDTO componentRequestDTO) {
        Component component = new Component();

        component.setName(componentRequestDTO.name());
        component.setDescription(componentRequestDTO.description());

        Component savedComponent = componentRepository.save(component);
        return mapToResponseDTO(savedComponent);
    }

    public ComponentResponseDTO updateComponent(Long id, ComponentRequestDTO componentrequestDTO) {
        Component existingComponent = findComponentEntityById(id);

        existingComponent.setName(componentrequestDTO.name());
        existingComponent.setDescription(componentrequestDTO.description());

        Component updatedComponent = componentRepository.save(existingComponent);
        return mapToResponseDTO(updatedComponent);
    }

    public void deleteComponent(Long id) {
        Component component = findComponentEntityById(id);
        componentRepository.delete(component);
    }

    private Component findComponentEntityById(Long id) {
        return componentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Component not found"));
    }

    private ComponentResponseDTO mapToResponseDTO(Component component) {
        return new ComponentResponseDTO(
                component.getComponentId(),
                component.getName(),
                component.getDescription()
        );
    }
}
