package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Condition;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.Status;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;
import uoc.edu.service.ConsoleService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleServiceTest {

    @Mock
    private ConsoleRepository consoleRepository;

    @Mock
    private ConsoleModelRepository consoleModelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RepairCaseRepository repairCaseRepository;

    @InjectMocks
    private ConsoleService consoleService;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoyModel;
    private ConsoleModel playStationModel;

    private User owner;
    private User secondOwner;

    private Console gameBoy;
    private Console playStation;

    private Condition condition;
    private Status status;

    @BeforeEach
    void setUp() {
        nintendo = createManufacturer(1L, "Nintendo", "JP");
        sony = createManufacturer(2L, "Sony", "JP");

        gameBoyModel = createConsoleModel(1L, "Game Boy", 1989, nintendo);
        playStationModel = createConsoleModel(2L, "PlayStation", 1994, sony);

        owner = createUser(1L);
        secondOwner = createUser(2L);

        condition = Condition.EXCELLENT;
        status = Status.AVAILABLE;

        gameBoy = createConsole(1L, owner, gameBoyModel, "GB-001", "JP", "Grey", condition, new BigDecimal("120.00"), status, "Original Game Boy");

        playStation = createConsole(2L, secondOwner, playStationModel, "PS-001", "JP", "Grey", condition, new BigDecimal("150.00"), status, "Original PlayStation");
    }

    // get all consoles test

    @Test
    void getAllConsolesReturnsConsoles() {
        when(consoleRepository.findAll()).thenReturn(List.of(gameBoy, playStation));

        List<ConsoleResponseDTO> result = consoleService.getAllConsoles();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(1L, result.getFirst().consoleId()),
                () -> assertEquals(1L, result.getFirst().ownerId()),
                () -> assertEquals(1L, result.getFirst().consoleModelId()),
                () -> assertEquals("Game Boy", result.getFirst().consoleModelName()),
                () -> assertEquals("Nintendo", result.getFirst().manufacturerName()),
                () -> assertEquals("GB-001", result.getFirst().serialNumber()),
                () -> assertEquals("JP", result.getFirst().region()),
                () -> assertEquals("Grey", result.getFirst().color()),
                () -> assertEquals(condition, result.getFirst().condition()),
                () -> assertEquals(new BigDecimal("120.00"), result.getFirst().estimatedValue()),
                () -> assertEquals(status, result.getFirst().status()),
                () -> assertEquals("Original Game Boy", result.getFirst().notes())
        );

        assertAll(
                () -> assertEquals(2L, result.get(1).consoleId()),
                () -> assertEquals(2L, result.get(1).ownerId()),
                () -> assertEquals(2L, result.get(1).consoleModelId()),
                () -> assertEquals("PlayStation", result.get(1).consoleModelName()),
                () -> assertEquals("Sony", result.get(1).manufacturerName()),
                () -> assertEquals("PS-001", result.get(1).serialNumber()),
                () -> assertEquals("JP", result.get(1).region()),
                () -> assertEquals("Grey", result.get(1).color()),
                () -> assertEquals(condition, result.get(1).condition()),
                () -> assertEquals(new BigDecimal("150.00"), result.get(1).estimatedValue()),
                () -> assertEquals(status, result.get(1).status()),
                () -> assertEquals("Original PlayStation", result.get(1).notes())
        );

        verify(consoleRepository).findAll();

        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(consoleModelRepository, userRepository, repairCaseRepository);
    }

    @Test
    void getAllConsolesReturnsEmptyList() {
        when(consoleRepository.findAll()).thenReturn(List.of());

        List<ConsoleResponseDTO> result = consoleService.getAllConsoles();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(consoleRepository).findAll();

        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(consoleModelRepository, userRepository, repairCaseRepository);
    }

    // get console by id test

    @Test
    void getConsoleByIdReturnsConsole() {
        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));

        ConsoleResponseDTO result = consoleService.getConsoleById(1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(1L, result.consoleId()),
                () -> assertEquals(1L, result.ownerId()),
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Game Boy", result.consoleModelName()),
                () -> assertEquals("Nintendo", result.manufacturerName()),
                () -> assertEquals("GB-001", result.serialNumber()),
                () -> assertEquals("JP", result.region()),
                () -> assertEquals("Grey", result.color()),
                () -> assertEquals(condition, result.condition()),
                () -> assertEquals(new BigDecimal("120.00"), result.estimatedValue()),
                () -> assertEquals(status, result.status()),
                () -> assertEquals("Original Game Boy", result.notes())
        );

        verify(consoleRepository).findById(1L);

        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(consoleModelRepository, userRepository, repairCaseRepository);
    }

    @Test
    void getConsoleByIdThrowsExceptionWhenConsoleDoesNotExist() {
        when(consoleRepository.findById(99L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.getConsoleById(99L));

        assertEquals("Console not found", exception.getMessage());

        verify(consoleRepository).findById(99L);

        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(consoleModelRepository, userRepository, repairCaseRepository);
    }

    // add console test

    @Test
    void addConsoleReturnsCreatedConsole() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 1L, "GB-002", "JP", "Black", new BigDecimal("140.00"), condition, status, "Modified Game Boy");

        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(consoleRepository.findBySerialNumber("GB-002")).thenReturn(Optional.empty());

        when(consoleRepository.save(any(Console.class))).thenAnswer(invocation -> {Console console = invocation.getArgument(0);console.setConsoleId(3L);return console;});

        ConsoleResponseDTO result = consoleService.addConsole(request);

        assertNotNull(result);
        assertAll(
                () -> assertEquals(3L, result.consoleId()),
                () -> assertEquals(1L, result.ownerId()),
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Game Boy", result.consoleModelName()),
                () -> assertEquals("Nintendo", result.manufacturerName()),
                () -> assertEquals("GB-002", result.serialNumber()),
                () -> assertEquals("JP", result.region()),
                () -> assertEquals("Black", result.color()),
                () -> assertEquals(condition, result.condition()),
                () -> assertEquals(new BigDecimal("140.00"), result.estimatedValue()),
                () -> assertEquals(status, result.status()),
                () -> assertEquals("Modified Game Boy", result.notes())
        );

        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(consoleRepository).findBySerialNumber("GB-002");
        verify(consoleRepository).save(any(Console.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void addConsoleSavesCorrectData() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 1L, "GB-003", "JP", "Yellow", new BigDecimal("160.00"), condition, status, "Game Boy with IPS screen");

        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(consoleRepository.findBySerialNumber("GB-003")).thenReturn(Optional.empty());
        when(consoleRepository.save(any(Console.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Console> captor = ArgumentCaptor.forClass(Console.class);

        consoleService.addConsole(request);

        verify(consoleRepository).save(captor.capture());

        Console savedConsole = captor.getValue();

        assertNotNull(savedConsole);

        assertAll(
                () -> assertNull(savedConsole.getConsoleId()),
                () -> assertSame(owner, savedConsole.getOwner()),
                () -> assertSame(gameBoyModel, savedConsole.getConsoleModel()),
                () -> assertEquals("GB-003", savedConsole.getSerialNumber()),
                () -> assertEquals("JP", savedConsole.getRegion()),
                () -> assertEquals("Yellow", savedConsole.getColor()),
                () -> assertEquals(condition, savedConsole.getCondition()),
                () -> assertEquals(new BigDecimal("160.00"), savedConsole.getEstimatedValue()),
                () -> assertEquals(status, savedConsole.getStatus()),
                () -> assertEquals("Game Boy with IPS screen", savedConsole.getNotes())
        );

        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(consoleRepository).findBySerialNumber("GB-003");

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenConsoleModelDoesNotExist() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 99L, "GB-002", "JP", "Black", new BigDecimal("140.00"), condition, status, "Modified Game Boy");

        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.addConsole(request));

        assertEquals("Console model not found", exception.getMessage());

        verify(consoleModelRepository).findById(99L);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(consoleRepository, userRepository, repairCaseRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenOwnerDoesNotExist() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(99L, 1L, "GB-002", "JP", "Black", new BigDecimal("140.00"), condition, status, "Modified Game Boy");

        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.addConsole(request));

        assertEquals("User not found", exception.getMessage());

        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(99L);

        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(consoleRepository, repairCaseRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenSerialNumberAlreadyExists() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 1L, "GB-001", "JP", "Black", new BigDecimal("140.00"), condition, status, "Modified Game Boy");

        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(consoleRepository.findBySerialNumber("GB-001")).thenReturn(Optional.of(gameBoy));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> consoleService.addConsole(request));

        assertEquals("Console with serial number GB-001 already exists", exception.getMessage());

        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(consoleRepository).findBySerialNumber("GB-001");
        verify(consoleRepository, never()).save(any(Console.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    //update console test
    @Test
    void updateConsoleReturnsUpdatedConsole() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(2L, 2L, "PS-002", "US", "White", new BigDecimal("200.00"), condition, status, "Updated console");

        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(2L)).thenReturn(Optional.of(playStationModel));
        when(userRepository.findById(2L)).thenReturn(Optional.of(secondOwner));
        when(consoleRepository.findBySerialNumber("PS-002")).thenReturn(Optional.empty());
        when(consoleRepository.save(gameBoy)).thenReturn(gameBoy);

        ConsoleResponseDTO result = consoleService.updateConsole(1L, request);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(1L, result.consoleId()),
                () -> assertEquals(2L, result.ownerId()),
                () -> assertEquals(2L, result.consoleModelId()),
                () -> assertEquals("PlayStation", result.consoleModelName()),
                () -> assertEquals("Sony", result.manufacturerName()),
                () -> assertEquals("PS-002", result.serialNumber()),
                () -> assertEquals("US", result.region()),
                () -> assertEquals("White", result.color()),
                () -> assertEquals(new BigDecimal("200.00"), result.estimatedValue()),
                () -> assertEquals(condition, result.condition()),
                () -> assertEquals(status, result.status()),
                () -> assertEquals("Updated console", result.notes())
        );

        verify(consoleRepository).findById(1L);
        verify(consoleModelRepository).findById(2L);
        verify(userRepository).findById(2L);
        verify(consoleRepository).findBySerialNumber("PS-002");
        verify(consoleRepository).save(gameBoy);

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void updateConsoleSavesCorrectData() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(2L, 2L, "PS-003", "US", "Black", new BigDecimal("230.00"), condition, status, "Console after repair");

        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(2L)).thenReturn(Optional.of(playStationModel));
        when(userRepository.findById(2L)).thenReturn(Optional.of(secondOwner));
        when(consoleRepository.findBySerialNumber("PS-003")).thenReturn(Optional.empty());
        when(consoleRepository.save(any(Console.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Console> captor = ArgumentCaptor.forClass(Console.class);
        consoleService.updateConsole(1L, request);

        verify(consoleRepository).save(captor.capture());

        Console updatedConsole = captor.getValue();

        assertNotNull(updatedConsole);

        assertAll(
                () -> assertSame(gameBoy, updatedConsole),
                () -> assertEquals(1L, updatedConsole.getConsoleId()),
                () -> assertSame(secondOwner, updatedConsole.getOwner()),
                () -> assertSame(playStationModel, updatedConsole.getConsoleModel()),
                () -> assertEquals("PS-003", updatedConsole.getSerialNumber()),
                () -> assertEquals("US", updatedConsole.getRegion()),
                () -> assertEquals("Black", updatedConsole.getColor()),
                () -> assertEquals(new BigDecimal("230.00"), updatedConsole.getEstimatedValue()),
                () -> assertEquals(condition, updatedConsole.getCondition()),
                () -> assertEquals(status, updatedConsole.getStatus()),
                () -> assertEquals("Console after repair", updatedConsole.getNotes())
        );

        verify(consoleRepository).findById(1L);
        verify(consoleModelRepository).findById(2L);
        verify(userRepository).findById(2L);
        verify(consoleRepository).findBySerialNumber("PS-003");

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void updateConsoleAllowsKeepingCurrentSerialNumber() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 1L, "GB-001", "JP", "Black", new BigDecimal("180.00"), condition, status, "Updated Game Boy");

        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(consoleRepository.findBySerialNumber("GB-001")).thenReturn(Optional.of(gameBoy));
        when(consoleRepository.save(gameBoy)).thenReturn(gameBoy);

        ConsoleResponseDTO result = consoleService.updateConsole(1L, request);

        assertNotNull(result);
        assertAll(
                () -> assertEquals(1L, result.consoleId()),
                () -> assertEquals("GB-001", result.serialNumber()),
                () -> assertEquals("Black", result.color()),
                () -> assertEquals(new BigDecimal("180.00"), result.estimatedValue()),
                () -> assertEquals("Updated Game Boy", result.notes())
        );

        verify(consoleRepository).findById(1L);
        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(consoleRepository).findBySerialNumber("GB-001");
        verify(consoleRepository).save(gameBoy);

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void updateConsoleThrowsExceptionWhenSerialNumberBelongsToAnotherConsole() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 1L, "PS-001", "JP", "Black", new BigDecimal("180.00"), condition, status, "Updated Game Boy");

        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(consoleRepository.findBySerialNumber("PS-001")).thenReturn(Optional.of(playStation));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> consoleService.updateConsole(1L, request));

        assertEquals("Trying to update with invalid serialNumber PS-001 belonging to console with id 1", exception.getMessage());
        assertEquals("GB-001", gameBoy.getSerialNumber());

        verify(consoleRepository).findById(1L);
        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(consoleRepository).findBySerialNumber("PS-001");
        verify(consoleRepository, never()).save(any(Console.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void updateConsoleThrowsExceptionWhenConsoleDoesNotExist() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 1L, "GB-002", "JP", "Black", new BigDecimal("180.00"), condition, status, "Updated Game Boy");

        when(consoleRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.updateConsole(99L, request));

        assertEquals("Console not found", exception.getMessage());

        verify(consoleRepository).findById(99L);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(consoleModelRepository, userRepository, repairCaseRepository);
    }

    @Test
    void updateConsoleThrowsExceptionWhenConsoleModelDoesNotExist() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(1L, 99L, "GB-002", "JP", "Black", new BigDecimal("180.00"), condition, status, "Updated Game Boy");

        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.updateConsole(1L, request));
        assertEquals("Console model not found", exception.getMessage());

        verify(consoleRepository).findById(1L);
        verify(consoleModelRepository).findById(99L);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(userRepository, repairCaseRepository);
    }

    @Test
    void updateConsoleThrowsExceptionWhenOwnerDoesNotExist() {
        ConsoleRequestDTO request = new ConsoleRequestDTO(99L, 1L, "GB-002", "JP", "Black", new BigDecimal("180.00"), condition, status, "Updated Game Boy");

        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.updateConsole(1L, request));

        assertEquals("User not found", exception.getMessage());

        verify(consoleRepository).findById(1L);
        verify(consoleModelRepository).findById(1L);
        verify(userRepository).findById(99L);
        verify(consoleRepository, never()).save(any(Console.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(repairCaseRepository);
    }

    // delete console test

    @Test
    void deleteConsoleDeletesConsole() {
        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));

        when(repairCaseRepository.existsByConsoleConsoleIdAndStatusNot(1L, RepairStatus.CLOSED)).thenReturn(false);

        consoleService.deleteConsole(1L);

        verify(consoleRepository).findById(1L);
        verify(repairCaseRepository).existsByConsoleConsoleIdAndStatusNot(1L, RepairStatus.CLOSED);
        verify(consoleRepository).delete(gameBoy);

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(consoleModelRepository, userRepository);
    }

    @Test
    void deleteConsoleThrowsExceptionWhenConsoleHasActiveRepairCases() {
        when(consoleRepository.findById(1L)).thenReturn(Optional.of(gameBoy));

        when(repairCaseRepository.existsByConsoleConsoleIdAndStatusNot(1L, RepairStatus.CLOSED)).thenReturn(true);

        ResourceInUseException exception = assertThrows(ResourceInUseException.class, () -> consoleService.deleteConsole(1L));

        assertEquals("Console with id 1 cannot be deleted because it has active repair cases", exception.getMessage());

        verify(consoleRepository).findById(1L);
        verify(repairCaseRepository).existsByConsoleConsoleIdAndStatusNot(1L, RepairStatus.CLOSED);
        verify(consoleRepository, never()).delete(any(Console.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(consoleModelRepository, userRepository);
    }

    @Test
    void deleteConsoleThrowsExceptionWhenConsoleDoesNotExist() {
        when(consoleRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleService.deleteConsole(99L));

        assertEquals("Console not found", exception.getMessage());

        verify(consoleRepository).findById(99L);
        verify(consoleRepository, never()).delete(any(Console.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(consoleModelRepository, userRepository, repairCaseRepository);
    }

    private Manufacturer createManufacturer(Long id, String name, String countryCode
    ) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setManufacturerId(id);
        manufacturer.setManufacturerName(name);
        manufacturer.setCountryCode(countryCode);
        return manufacturer;
    }

    private ConsoleModel createConsoleModel(Long id, String name, Integer releaseYear, Manufacturer manufacturer
    ) {
        ConsoleModel consoleModel = new ConsoleModel();
        consoleModel.setConsoleModelId(id);
        consoleModel.setConsoleModelName(name);
        consoleModel.setReleaseYear(releaseYear);
        consoleModel.setManufacturer(manufacturer);
        return consoleModel;
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Console createConsole(Long id, User owner, ConsoleModel consoleModel, String serialNumber, String region, String color, Condition condition, BigDecimal estimatedValue, Status status, String notes
    ) {
        Console console = new Console();
        console.setConsoleId(id);
        console.setOwner(owner);
        console.setConsoleModel(consoleModel);
        console.setSerialNumber(serialNumber);
        console.setRegion(region);
        console.setColor(color);
        console.setCondition(condition);
        console.setEstimatedValue(estimatedValue);
        console.setStatus(status);
        console.setNotes(notes);
        return console;
    }
}
