package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.Console;

import java.util.Optional;

public interface ConsoleRepository extends JpaRepository<Console, Long> {
    boolean existsByConsoleModelConsoleModelId(Long consoleModelId);
    Optional<Console> findBySerialNumber(String serialNumber);
}
