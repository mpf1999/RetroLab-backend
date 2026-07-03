package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.Manufacturer;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
}
