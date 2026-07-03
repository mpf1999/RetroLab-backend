package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.ConsoleModel;

public interface ConsoleModelRepository extends JpaRepository<ConsoleModel, Long> {
    boolean existsByManufacturerManufacturerId(Long manufacturerId);
}
