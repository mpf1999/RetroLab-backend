package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.Component;

public interface ComponentRepository extends JpaRepository<Component, Long> {

}
