package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.ComponentTest;

import java.util.List;

public interface ComponentTestRepository extends JpaRepository<ComponentTest, Long> {

    List<ComponentTest> findByRepairCaseId(Long repairCaseId);

    List<ComponentTest> findByComponentId(Long componentId);
}
